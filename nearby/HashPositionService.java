

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		long l=lt;
		//这里value是  ((纬度<<32)|经度)
		value.put(code,((l<<32)|lg));
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
	public Map<Object,Long> search(double lng,double lat,int distance){
		
		return search(getLngCode(lng),getLatCode(lat),distance);
	}
	
	public Map<Object,Long> search(int cntX,int cntY,int distance){
		int dist=(int)(distance/EARTH_RATIO);
		
		//计算纬度开始，结束值
		int startY=((cntY-dist)>>LOW_BITS),endY=((cntY+dist)>>LOW_BITS);
		
		dist/=Math.sin((cntY*Math.PI/LATITUDE_MAXIMIZE));		
		//计算经度开始，结束值
		int startX=((cntX-dist)>>LOW_BITS),endX=((cntX+dist)>>LOW_BITS);
		
		Map<Object,Long> map=new HashMap<Object,Long>();
		//循环遍历蓝色格子
		for(int lt=startY;lt<=endY;lt++){
			for(int lg=startX;lg<=endX;lg++){
				int key=((lt<<HIGH_BITS)|lg);
	    
				Map<Object,Long> value=table.get(key);
				if(value!=null){
					//遍历二级点
					for(Map.Entry<Object, Long> it:value.entrySet()){
						long t=it.getValue();
	      
						if(getDistance((int)(t&DATA_MASK),(int)(t>>32),cntX,cntY)<distance){
							map.put(it.getKey(), t);
						}
					}
				}
			}
		}
	  
		return map;
	}
	
	public static void main(String args[])throws Exception{
		HashPositionService pos=new HashPositionService();
		double objX=114.507777;
		double objY=38.038064;
		System.out.println("lng: "+objX+", lat: "+objY);
		int distance=1000;
		Set<Object> sets=new HashSet<Object>();
		FileInputStream fis=new FileInputStream("D:\\sss\\pos.txt");
		byte data[]=new byte[64];
		while(fis.read(data,0,39)==39){
			String code=new String(data,0,16);
			double lng=Double.parseDouble(new String(data,17,10));
			double lat=Double.parseDouble(new String(data,28,9));
			if(Position.getDistance(lng,lat,objX,objY)<distance){
				sets.add(code);
			}
//			System.out.println("code: "+code+" lng: "+lng+", lat: "+lat);
			pos.insert(lng, lat, code);
		}
		fis.close();
		System.out.println(sets.size());
		
		
		long timeStart=System.currentTimeMillis();
		Map<Object,Long> result=null;
		for(int g=0;g<1000000;g++){
			result=pos.search(objX,objY,distance);
		}
		long timeEnd=System.currentTimeMillis();
		System.out.println("self useTime: "+(timeEnd-timeStart)+"\r\n\r\n");
		for(Map.Entry<Object, Long> it:result.entrySet()){
			System.out.println("Park: "+it.getKey()+", Distance: "+getDistance(getLngCode(objX),getLatCode(objY),(int)(it.getValue()&DATA_MASK),(int)((it.getValue()>>32))));
		}
		
	}
}
