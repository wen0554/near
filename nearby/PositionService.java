

import java.util.Map;

public abstract class PositionService {
	//2^30
	protected static final int MAXIMIZE=0x40000000;
	protected static final int DATA_BITS=30;
	
	
	public static final int LONGITUDE_MAXIMIZE=MAXIMIZE;
	public static final int LATITUDE_MAXIMIZE=(MAXIMIZE>>1);

	
	protected static final double EARTH_PERIMETER=6378.137*2000*Math.PI; 
	protected static final double EARTH_DISTANCE=EARTH_PERIMETER/360;
	
	protected static final double DATA_RATIO=MAXIMIZE/360.0;
	protected static final double EARTH_RATIO=EARTH_PERIMETER/MAXIMIZE;
	

	//浮点型经纬度转换为整型
	public static int getLngCode(double pos){
		return (int)(DATA_RATIO*(pos+180));
	}
	//整型经纬度转换浮点型
	public static double getLongitude(int code){
		return code/DATA_RATIO-180;
	}
	//根据浮点纬度计算编码
	public static int getLatCode(double lat){
		return (int)(DATA_RATIO*(lat+90));
	}
	//根据编码计算浮点纬度
	public static double getLatitude(int code){
		return code/DATA_RATIO-90;
	}
	//计算两点间距离(单位：米，勾股定理)
	public static int getDistance(double lng1,double lat1,double lng2,double lat2){
		lng1-=lng2;
		//修正曲面引起的误差 r=R*cos(纬度)
		lng1*=Math.cos((lat1+lat2)*Math.PI/360);
		lat1-=lat2;
		return (int)(Math.sqrt(lng1*lng1+lat1*lat1)*EARTH_DISTANCE);
	}
	//计算两点间距离(单位：米，勾股定理)
	public static int getDistance(int lng1,int lat1,int lng2,int lat2){
		long lg=lng1-lng2;
		//修正曲面引起的误差 r=R*cos(纬度-90)=R*sin(纬度)
		lg*=Math.sin(((lat1+lat2)*Math.PI/LATITUDE_MAXIMIZE/2));
		long lt=lat1-lat2;
		return (int)(Math.sqrt((lg*lg)+((lt*lt)))*EARTH_RATIO);
	}
	
	
	//插入对象
	public abstract void insert(double lng,double lat,Object code);
	
	public abstract void insert(int lg,int lt,Object code);
	//删除对象
	public abstract void remove(double lng,double lat,Object code);
	public abstract void remove(int lg,int lt,Object code);
	//查询对象
	public abstract  Map<Object,Long> search(double lng,double lat,int distance);
	
	public abstract Map<Object,Long> search(int cntX,int cntY,int distance);

	
	
	 //Geohash 经度编码
	public static int getLngCode2(double lng){
       int result=0;
       double start=-180,end=180,midd;

       for(int i=DATA_BITS;i>0;i--){
            result<<=1;
            midd=(start+end)/2;
            if(lng>midd){
                 result|=1;
                 start=midd;
            }
           else{
               end=midd;
            }
       }
       return result;
      }
     
    //Geohash 纬度编码
	public static int getLatCode2(double lat){
       int result=0;
       double start=-90,end=90,midd;
       for(int i=DATA_BITS-1;i>0;i--){
    	   result<<=1;
           midd=(start+end)/2;
           if(lat>midd){
                result|=1;
                start=midd;
           }
          else{
              end=midd;
           }
       }
       return result;
      }
    
	public static void main(String args[]) throws Exception{
		int lg=getLngCode(114.507777);
		System.out.println(lg);
		lg=getLngCode2(114.507777);
		System.out.println(lg);
		double lng=getLongitude(lg);
		System.out.println(lng);
		
		int lt=getLatCode(38.038064);
		System.out.println(lt);
		lt=getLatCode2(38.038064);
		System.out.println(lt);
		double lat=getLatitude(lt);
		System.out.println(lat);
	
	}
	
}
