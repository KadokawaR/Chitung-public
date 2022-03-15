package lielietea.mirai.plugin.administration.statistics.MPSEHandler;

import java.util.ArrayList;
import java.util.List;

public class DataList {
    List<Data> datas;

    DataList(Data data){
        this.datas = new ArrayList<>();
        this.datas.add(data);
    }

    DataList(){
        this.datas = new ArrayList<>();
    }

    public void addDataIntoDatas(Data data){
        this.datas.add(data);
    }

    public List<Data> getDatas() {
        return datas;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }
}
