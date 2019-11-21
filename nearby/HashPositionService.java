

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashPositionService extends  PositionService{
	
	private Map<Integer,Map<Object,Long>> table=new HashMap<Integer,Map<Object,Long>>();
	
	protected static final int DATA_MASK=0x3FFFFFFF;
	protected static final int HIGH_MASK=0xFFFF;
	protected static final int HIGH_BITS=16;
	protected static final int LOW_BITS=14;
	
	//插入对象
	public void insert(double lng,double lat,Object code){
		insert(getLngCode(lng),getLatCode(lat),code);
	}
	
	public void insert(int lg,int lt,Object code){
			
		int key=((lt>>LOW_BITS)<<HIGH_BITS)|(lg>>LOW_BITS);
	  
		Map<Object,Long> value=table.get(key);
		if(value==null){
			value=new HashMap<Object,Long>();
			table.put(key, value);
		}

		//这里value是  ((纬度<<32)|经度)
		value.put(code,((((long)lt)<<32)|lg));
	}
	//删除对象
	public void remove(double lng,double lat,Object code){
		remove(getLngCode(lng),getLatCode(lat),code);
	}
	public void remove(int lg,int lt,Object code){
			
		int key=((lt>>LOW_BITS)<<HIGH_BITS)|(lg>>LOW_BITS);
	  
		Map<Object,Long> value=table.get(key);
		if(value!=null){
			value.remove(code);
			//不再有任何对象，直接删除一级点
			if(value.size()==0){
				table.remove(key);
			}
		}
	}
	//查询对象
	public List<Point> search(double lng,double lat,int distance){
		
		return search(getLngCode(lng),getLatCode(lat),distance);
	}
	
	public List<Point> search(int lg,int lt,int distance){
		int dist=(int)(distance/EARTH_RATIO);
		
		//计算纬度开始，结束值
		int startY=((lt-dist)>>LOW_BITS),endY=((lt+dist)>>LOW_BITS);
		
		dist/=Math.sin((lt*Math.PI/LATITUDE_MAXIMIZE));		
		//计算经度开始，结束值
		int startX=((lg-dist)>>LOW_BITS),endX=((lg+dist)>>LOW_BITS);
		
		List<Point> list=new ArrayList<Point>();
		//循环遍历蓝色格子
		for(int i=startY;i<=endY;i++){
			for(int j=startX;j<=endX;j++){
				int key=((i<<HIGH_BITS)|j);
	    
				Map<Object,Long> value=table.get(key);
				if(value!=null){
					//遍历二级点
					for(Map.Entry<Object, Long> it:value.entrySet()){
						long p=it.getValue();
						int g=(int)(p&DATA_MASK),t=(int)(p>>32);
						
						int d=getDistance(g,t,lg,lt);
						if(d<distance){
							list.add(new Point(g,t,d,it.getKey()));
						}
					}
				}
			}
		}
	  
		return list;
	}
}
