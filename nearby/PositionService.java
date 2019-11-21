

import java.util.Comparator;
import java.util.List;

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
	
	public class Point implements Comparable<Point>{
		public int lngCode;
		public int latCode;
		public int distance;
		public Object code;
		public Point() {
			
		}
		public Point(int lngCode, int latCode) {
			this.lngCode = lngCode;
			this.latCode = latCode;
		}
		public Point(int lngCode, int latCode, int distance) {
			this.lngCode = lngCode;
			this.latCode = latCode;
			this.distance = distance;
		}
		public Point(int lngCode, int latCode, int distance,Object code) {
			this.lngCode = lngCode;
			this.latCode = latCode;
			this.distance = distance;
			this.code = code;
		}
		public void setLngCode(int lngCode) {
			this.lngCode = lngCode;
		}
		public void setLatCode(int latCode) {
			this.latCode = latCode;
		}
		public void setDistance(int distance) {
			this.distance = distance;
		}
		public void setCode(Object code) {
			this.code = code;
		}
		public int getLngCode() {
			return lngCode;
		}
		public int getLatCode() {
			return latCode;
		}
		public int getDistance() {
			return distance;
		}
		public Object getCode() {
			return code;
		}
		
		public int compareTo(Point o) {
			if(this.distance>o.distance){
				return 1;
			}
			else if(this.distance<o.distance) {
				return -1;
			}
			else{
				return 0;
			}
		}
		public boolean equals(Object obj){
			if(obj==null || !(obj instanceof Point)){
				return false;
			}
			return ((Point)obj).distance==this.distance;
		}
	}
	//插入对象
	public abstract void insert(double lng,double lat,Object code);
	public abstract void insert(int lg,int lt,Object code);
	//删除对象
	public abstract void remove(double lng,double lat,Object code);
	public abstract void remove(int lg,int lt,Object code);
	//查询对象,distance距离，单位：米; 返回: Map<code, Node>
	public abstract  List<Point> search(double lng,double lat,int distance);
	public abstract List<Point> search(int lg,int lt,int distance);
}
