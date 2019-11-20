import java.util.HashMap;
import java.util.Map;


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
	
	public Map<Object,Long> search(double lng,double lat,int distance){

		return search(getLngCode(lng), getLatCode(lat), distance);
	}
	
	public Map<Object,Long> search(int cntX,int cntY,int distance){
		double dist=distance/EARTH_RATIO;
		int distY=(int)dist;

		int distX=(int)(dist/Math.sin((cntY*Math.PI/LATITUDE_MAXIMIZE)));
		long min=merge(cntX-distX,cntY-distY);
		long max=merge(cntX+distX,cntY+distY);
		
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

		Map<Object,Long> map=new HashMap<Object,Long>();
		while(startIndex<=endIndex){
			Node n=this.nodeTable[startIndex++];
			int pl[]=split(n.postion);
			if(getDistance(pl[0],pl[1],cntX,cntY)<distance){
				long l=pl[1];
				map.put(n.code,((l<<32)|pl[0]));
			}
		}
		return map;
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
	
	
	public static void main(String args[])throws Exception{
		int len=16;
//		int result[]=new int[32];
		
		for(int lat=len-1;lat>=0;lat--){
			for(int lng=0;lng<len;lng++){
				int t=0;
				for(int i=0;i<len;i++){
					t=((t<<1)|(lng>>(len-1-i))&1);
					t=((t<<1)|(lat>>(len-1-i))&1);
					
				}
				if(t<10){
					System.out.print("00");
				}
				else if(t<100){
					System.out.print("0");
				}
				System.out.print(t+" ");
			}
			System.out.println();
		}
	}
}
