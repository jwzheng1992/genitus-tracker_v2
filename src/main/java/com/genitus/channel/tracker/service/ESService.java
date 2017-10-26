package com.genitus.channel.tracker.service;

import com.alibaba.fastjson.JSONObject;
import com.genitus.channel.tracker.client.ESClient;
import com.genitus.channel.tracker.model.parameter.Parameter;
import com.genitus.channel.tracker.model.esResult.Result;
import com.genitus.channel.tracker.util.Utils;
import com.genitus.channel.tracker.util.comparator.Comparator_TimeStamp;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ESService extends AbstractIdleService {
    protected void startUp() throws Exception {

    }

    protected void shutDown() throws Exception {

    }

    private Map<String, ESClient> esClientMap;        //依赖注入
    private static Logger logger = LoggerFactory.getLogger(ESService.class);
    //this constructor method is used to test the performance of getting data from elasticsearch
    public ESService(Map<String, ESClient> esClientMap){
        this.esClientMap= esClientMap;
    }

    /**
     * close es client
     */
    public void closeESClient(){
        Iterator< Map.Entry<String,ESClient>> iter =  esClientMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,ESClient> entry= iter.next();
            entry.getValue().close();
        }
    }




    /**
     * 在这个方法里面 根据查询条件 分别是调用searchInSingleCluster和searchInMultiCluster两个方法
     * @param jsonStr
     * @return
     */
    public String search(String jsonStr){
        Parameter param = JSONObject.parseObject(jsonStr, Parameter.class);
        String searchResult="";
        if (param.getCluster().equals("single"))
            searchResult=searchInSingleCluster(param);
        if (param.getCluster().equals("multi"))
            searchResult=searchInMultiCluster(param);
        return searchResult;
    }


    /**
     * 单集群搜索
     * @param param
     * @return
     */
    private String searchInSingleCluster(Parameter param){
        logger.info("This is a single cluster query request.");
        System.out.println("Query info print:");
        HashMap<String,Object> query = param.getQuery();
        Utils.printlnMap(query);

        Iterator< Map.Entry<String,ESClient>> iter =  esClientMap.entrySet().iterator();
        /* ESClient esClient=new ESClient();
       String city="";
        while(iter.hasNext()){
            Map.Entry<String,ESClient> entry = iter.next();
            city = entry.getKey();
            esClient = entry.getValue();
        }*/
       String city = param.getContent().getCity();
        ESClient esClient = esClientMap.get(city);

        //在这里增加一个逻辑 对于在nc集群上找的数据 是包含nc和sc集群的 需要把sc的数据给过滤掉
        if(city.equals("nc")){
            //采取的策略是一次性返回1w条数据 将这些数据过滤后返回，返回的可能会少于1w条

        }




        //从参数中提取filterTimeStamp 单集群搜索 因此只有一个timestamp
        String filterTimeStamp = param.getTimeStamp().split(",")[1];

        HashMap<String,Object> map = search(esClient,param,filterTimeStamp);
        SearchHit[] hits = (SearchHit[])map.get("hits");
        filterTimeStamp = (String)map.get("timestamp");
        Result result = Utils.saveAsJsonObject(hits);
        result.setTimestamp(city+","+filterTimeStamp);
        System.out.println("In single cluster, retursSize is: "+(long)map.get("returnSize"));
        result.setCount((long)map.get("returnSize"));

        Gson gson = new Gson();
        String searchResult=gson.toJson(result);
/*        for (SearchHit hit:hits)
            System.out.println(hit.sourceAsString());*/
        return searchResult;
    }





    /**
     * 多集群搜索
     * @param param
     * @return
     */
    private String searchInMultiCluster(Parameter param){
        logger.info("This is a multi cluster query request.");
        System.out.println("Query info print:");
        HashMap<String,Object> query = param.getQuery();
        Utils.printlnMap(query);

        String searchResult="";
        ArrayList<SearchHit> arrayList = new ArrayList<SearchHit>();
        Iterator< Map.Entry<String,ESClient>> iter =  esClientMap.entrySet().iterator();

        //从参数中提取filterTimeStamp 多集群搜索 因此构建一个timestampMap 每次调用search的时候 将需要的timestamp传进去
        // nc,2017-09-11T07:01:39.325Z&sc,2017-09-11T07:01:39.325Z
        String timestamp = param.getTimeStamp();
      //  String[] ts = timestamp.split("&"); changge
        String[] ts = timestamp.split("and");
        HashMap<String,String> filterTimeStampMap = new HashMap<String,String>();
        filterTimeStampMap.put("nc",ts[0].split(",")[1]);
        filterTimeStampMap.put("sc",ts[1].split(",")[1]);

        LinkedList<String> list = new LinkedList<String>();
        long returnSize=0;
        while(iter.hasNext()){
            Map.Entry<String,ESClient> entry = iter.next();
            String city = entry.getKey();
            ESClient esClient = entry.getValue();
            HashMap<String,Object> map =search(esClient,param,filterTimeStampMap.get(city));
            SearchHit[] hits = (SearchHit[])map.get("hits");
            String filterTimeStamp = (String) map.get("timestamp");
            returnSize += (long)map.get("returnSize");
            list.add(city+","+filterTimeStamp);
            arrayList.addAll(Arrays.asList(hits));
        }
        Collections.sort(arrayList,new Comparator_TimeStamp());

        //get hits in specific position as return data
        int startPage = param.getStartIndex();
        int pageSize = param.getSize();
        int from = (startPage-1)*pageSize+1;
        int to = startPage*pageSize;
        SearchHit[] hits = getHits(arrayList,from,to);
        Result result = Utils.saveAsJsonObject(hits);
        result.setTimestamp(list.get(0)+"and"+list.get(1));  //changge
        System.out.println("In multi cluster, retursSize is: "+returnSize);
        result.setCount(returnSize);

        Gson gson = new Gson();
        searchResult=gson.toJson(result);
/*        for (SearchHit hit:hits)
            System.out.println(hit.sourceAsString());*/
        return searchResult;
    }


    //返回from到to的所有的数据 包括from和to 但是一种可能的情况是：arraylist很小 小于to-from的大小，
    private SearchHit[] getHits(ArrayList<SearchHit> arrayList,int from,int to){

        SearchHit[] hits=new SearchHit[to-from+1];
        for (int i=from;i<=to;i++){
            if ((i-1)<arrayList.size()) //防止越界 防止上面的情况发生
                hits[i-from]=arrayList.get(i-1);
        }

        return hits;
    }

    /**
     * 单集群和多集群情况下：搜索的时候不需要使用filterTimeStamp，但会给filterTimeStamp赋值；分页的时候，会使用filterTimeStamp
     * @param esClient
     * @param param
     * @param filterTimeStamp
     * @return
     */
    private  HashMap<String,Object> search(ESClient esClient, Parameter param,String filterTimeStamp ){
        System.out.println("In search method, filterTimeStamp is:"+filterTimeStamp);
        SearchHit[] hits;
        int fromPages = param.getStartIndex();  //Note：startIndex is the pages, startIndex=0 indicates it is first page
        int pageSize=param.getSize();
        long returnSize=0;
        SearchResponse searchResponse=new SearchResponse();
        HashMap<String,Object> map = new HashMap<String,Object>();
        if(param.getSearch()){ //搜索
            LinkedList<Object> list = new LinkedList<Object> ();  //返回的List中第一个是SearchResponse 第二个是集群中最近的时间戳
            if(param.getCluster().equals("single"))
                list = esClient.getSearchResponse(param);
            if (param.getCluster().equals("multi"))
                list = esClient.getSearchResponse(param.getQuery(),1,fromPages*pageSize);
            filterTimeStamp = (String )list.get(0);   //timestamp需要被返回
            searchResponse = (SearchResponse)list.get(1);
            hits=searchResponse.getHits().getHits();

            returnSize=(long) list.get(2);  //需要返回符合条件的大小
            System.out.println("集群中最新的timestamp是: "+filterTimeStamp);   // timestamp value should be returned because it will be used when we turn page
            map.put("hits",hits);
            map.put("timestamp",filterTimeStamp);
            map.put("returnSize",returnSize);
        }
        else{ //翻页
            System.out.println("这里过滤使用的filterTimeStamp是："+filterTimeStamp);
            LinkedList<Object> list = new LinkedList<Object> ();
            if(param.getCluster().equals("single"))
                list =esClient.turnPage(param,filterTimeStamp);
            if (param.getCluster().equals("multi"))
                list =esClient.turnPage( param.getQuery(), 1,  fromPages*pageSize, filterTimeStamp);   //如果是在多集群上搜索 则需要在每个集群上都返回第1到第fromPages*pageSize的数据
            searchResponse = (SearchResponse)list.get(0);
            returnSize=(long) list.get(1);
            hits=searchResponse.getHits().getHits();
            map.put("hits",hits);
            map.put("timestamp",filterTimeStamp);
            map.put("returnSize",returnSize);
        }
        return map;
    }
}
