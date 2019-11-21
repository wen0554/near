import java.util.ArrayList;
import java.util.List;


public class GeohashArrayPositionService extends PositionService{
	
	public GeohashArrayPositionService(){
		this.nodeTable=new Node[16];
		this.count=0;
		
	}
	
	private static long merge(int lng,int lat){
		long t=0;
		for(int i=0;i<DATA_BITS;i++){
			t=((t<<1)|(lat>>(DATA_BITS-1-i))&1);
			t=((t<<1)|(lng>>(DATA_BITS-1-i))&1);
		}
		return t;
	}
	
	private static int[] split(long code){
		long lng=0,lat=0;
		for(int i=0;i<DATA_BITS;i++){
			lat=((lat<<1)|(code>>(((DATA_BITS-i)<<1)-1))&1);
			lng=((lng<<1)|(code>>(((DATA_BITS-i)<<1)-2))&1);
		}
		return new int[]{(int)lng,(int)lat};
	}
	
	public void insert(double lng,double lat,Object code){
		insert(getLngCode(lng),getLatCode(lat),code);
	}
	
	public void insert(int lng,int lat,Object code){
		long key=merge(lng, lat);
		
		if(this.nodeTable.length==this.count){
			Node nd[]=new Node[this.count<<1];
			System.arraycopy(this.nodeTable, 0, nd, 0, this.count);
			this.nodeTable=nd;
		}
		int start=0,end=count-1,midd;
		while(start<=end){
			midd=((start+end)>>1);
			Node n=this.nodeTable[midd];
			if(key>n.postion){
				start=midd+1;
			}
			else if(key<n.postion){
				end=midd-1;
			}
			else{
				start=midd;
				break;
			}
		}
		for(int i=this.count;i>start;i--){
			this.nodeTable[i]=this.nodeTable[i-1];
		}
		this.nodeTable[start]=new Node(key,code);
		this.count++;
	}
	
	public void remove(double lng,double lat,Object code){
		//无法删除
	}
	public void remove(int lng,int lat,Object code){
		//无法删除
	}
	
	public List<Point> search(double lng,double lat,int distance){

		return search(getLngCode(lng), getLatCode(lat), distance);
	}
	
	public List<Point> search(int lg,int lt,int distance){
		double dist=distance/EARTH_RATIO;
		int distY=(int)dist;

		int distX=(int)(dist/Math.sin((lt*Math.PI/LATITUDE_MAXIMIZE)));
		long min=merge(lg-distX,lt-distY);
		long max=merge(lg+distX,lt+distY);
		
		int start,end,midd;
		
		start=0;
		end=count-1;
		while(start<=end){
			midd=((start+end)>>1);
			Node n=this.nodeTable[midd];
			if(min>n.postion){
				start=midd+1;
			}
			else if(min<n.postion){
				end=midd-1;
			}
			else{
				start=midd;
				break;
			}
		}
		int startIndex=start;
		
		start=0;
		end=count-1;
		while(start<=end){
			midd=((start+end)>>1);
			Node n=this.nodeTable[midd];
			if(max>n.postion){
				start=midd+1;
			}
			else if(max<n.postion){
				end=midd-1;
			}
			else{
				start=midd;
				break;
			}
		}
		int endIndex=start;

		List<Point> list=new ArrayList<Point>();
		while(startIndex<=endIndex){
			Node n=this.nodeTable[startIndex++];
			int pl[]=split(n.postion);
			int d=getDistance(pl[0],pl[1],lg,lt);
			if(d<distance){
				long l=pl[1];
				list.add(new Point(pl[0],pl[1],d,n.code));
			}
		}
		return list;
	}
	
	private Node[] nodeTable;
	private int count;
	
	private static class Node{
		private long postion;
		private Object code;
		
		public Node(long postion, Object code) {
			this.postion = postion;
			this.code = code;
		}
	}
	
}
