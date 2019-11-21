import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class GeohashBtreePositionService extends PositionService{
	
	
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
		if(!table.containsKey(key)){
			table.put(key, code);
			insert(key);
		}
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
		int distY=(int)(distance/EARTH_RATIO);

		int distX=(int)(distY/Math.sin((lt*Math.PI/LATITUDE_MAXIMIZE)));
		long min=merge(lg-distX-1,lt-distY-1);
		long max=merge(lg+distX+1,lt+distY+1);
		Set<Long> sets=search(min,max);
		List<Point> list=new ArrayList<Point>();
		for(Long key:sets){
			int pl[]=split(key);
//			if(getDistance(getLongitude(pl[0]),getLatitude(pl[1]),lng,lat)<distance){
			int d=getDistance(pl[0],pl[1],lg,lt);
			if(d<distance){
				list.add(new Point(pl[0],pl[1],d,table.get(key)));
			}
		}
		return list;
	}
	
	/**根节点*/
	private Node root=null;
	private Map<Long,Object> table=new HashMap<Long,Object>();
	
	private static final int LEFT_HIGH=1;
	private static final int RIGHT_HIGH=-1;
	private static final int EQUAL_HIGH=0;
	
	
	private boolean insert(long key){
	
		Node t=root;
		if(t==null){
			/**将值复制给根节点，其父节点为空*/
			root=new Node(key,null);
			return true;
		}
	
		int cmp=0;
		Node parent;/**用来保存t的父节点*/
		
		/**查找结点应存放的位置*/
		do{
			parent=t;
			cmp=t.compareTo(key);
			if(cmp<0){
				t=t.left;
			}else if(cmp>0){
				t=t.right;
			}else{
				return false;
			}
		}while(t!=null);
		
		/**将结点存放在相应的位置*/
		Node child=new Node(key,parent);
		if(cmp<0){
			parent.left=child;
		}else{
			parent.right=child;
		}
	
	
		/**开始回溯，为根节点修改balabce，以便进行相应的调整*/
		while(parent!=null){
			cmp=parent.compareTo(key);
			if(cmp<0){
				parent.balance++;
			}else{
				parent.balance--;
			}
		
			if(parent.balance==0){/**从这以上的结点都不需要修改了，也不需要旋转了*/
				break;
			}
	
			if(parent.balance>=2){
				leftBanance(parent);
				break;
			}
			if(parent.balance<=-2){
				rightBalance(parent);
				break;
			}
			parent=parent.parent;
		}

		return true;
	}
	
	
	private Set<Long> search(long min,long max){
		Set<Long> sets=new HashSet<Long>();

		Node tmp=root;
		Stack<Node> stack=new Stack<Node>();
		while(tmp!=null||!stack.isEmpty()){
			while(tmp!=null){
				stack.push(tmp);
				//节点太小，没必要再查询其左节点
				if(tmp.compareTo(min)>0){
					break;
				}

				tmp=tmp.left;
			}
			tmp=stack.pop();
			//节点太大，没必要在向右查找
			if(tmp.compareTo(max)<0){
				break;
			}
			//将匹配的车牌插入到返回列表
			sets.add(tmp.key);
			tmp=tmp.right;
		}
		
		return sets;
	}
	/** 
     * 左平衡操作，即结点t的不平衡是因为左子树过深 
     *  
     * 1、如果新的结点插入到p的左孩子的左子树中，则直接进行右旋操作即可 
     *              t                                    lc 
     *             /  \             右旋操作                                                    /  \ 
     *            lc   rc       ------------->        lcl   t 
     *           /  \                                /    /  \ 
     *          lcl  lcr                           lcll  lcr rc  
     *          / 
     *         lcll 
     *          
     * 2、如果新的结点插入到p的左孩子的右子树中，则需要进行分情况讨论 
     *  
     *  情况a：当p的左孩子的右子树根节点的balance = RIGHT_HIGH 
     *  
     *          1                       1                           4 
     *         /  \                    /  \                        /  \ 
     *        2    6       左旋                             4    6         右旋                                     2    1 
     *       /  \       ------->     /  \        -------->       /    /  \ 
     *      3    4                  2    5                      3    5    6 
     *            \                / 
     *             5              3 
     *  
     *  
     *  情况b：当p的左孩子的右子树根节点的balance = LEFT_HIGH 
     *  
     *          1                       1                           4 
     *         /  \                    /  \                        /  \ 
     *        2    6        左旋                           4    6       右旋                                          2    1 
     *       /  \       ------->     /          -------->        / \    \ 
     *      3    4                  2                           3   5    6 
     *          /                  / \ 
     *         5                  3   5 
     *  
     *  情况c：当p的左孩子的右子树根节点的balance = EQUAL_HIGH 
     *  
     *          1                       1                           4 
     *         /  \                    /  \                        /  \ 
     *        2    7       左旋                              4    7        右旋                                       2     1 
     *       /  \       ------->     / \         -------->       / \   / \ 
     *      3    4                  2   6                       3   5  6  7 
     *          / \                / \ 
     *         5   6              3   5 
     * */ 
	private void leftBanance(Node t){
	
		Node lc=t.left,rd;
		switch(lc.balance){
			case LEFT_HIGH:/**新结点插入到t的左孩子的左子树上，需要单右旋处理*/
				lc.balance=EQUAL_HIGH;
				t.balance=EQUAL_HIGH;
				right_Rotate(t);
				break;
		
			case RIGHT_HIGH:/**新结点插入到t的左孩子的右子树上，需要双旋处理*/
				rd=lc.right;
				switch(rd.balance){
					case LEFT_HIGH:
						lc.balance=EQUAL_HIGH;
						t.balance=RIGHT_HIGH;
						break;
		
					case RIGHT_HIGH:
						lc.balance=LEFT_HIGH;
						t.balance=EQUAL_HIGH;
						break;
					case EQUAL_HIGH:
						t.balance=EQUAL_HIGH;
						lc.balance=EQUAL_HIGH;
						break;
				}
				rd.balance=EQUAL_HIGH;
				/**对t的左子树进行左旋处理*/
				left_Rotate(t.left);
				/**对t进行右旋处理*/
				right_Rotate(t);
				break;
		}
	}
	
	/** 
     * 右平衡操作，即结点t的不平衡是因为右子树过深 
     *  
     * 1、如果新的结点插入到p的右孩子的右子树中，则直接进行左旋操作即可 
     *  
     *          p                                           r 
     *        /   \                                        /  \ 
     *       l     r               左旋操作                                                               p   rr 
     *           /   \          ----------->             / \    \ 
     *          rl    rr                                l   rl   rrr 
     *                 \ 
     *                  rrr 
     *  
     *  
     * 2、如果新的结点插入到p的右孩子的左子树中，则需要进行分情况讨论 
     *  
     *  情况a：当p的右孩子的左子树根节点的balance = LEFT_HIGH 
     *  
     *          1                       1                            4 
     *         /  \                    /  \                        /   \ 
     *        2    3          右旋                    2    4            左旋                       1     3 
     *            /  \     ------->       /  \      ------->    /  \     \ 
     *           4    5                  6    3                2   6      5     
     *          /                              \ 
     *         6                                5 
     *  
     * 情况b：当p的右孩子的左子树根节点的balance = RIGHT_HIGH 
     *   
     *           1                       1                           4 
     *         /  \                    /  \                        /   \ 
     *        2    3          右旋                    2    4         左旋                                  1     3 
     *            /  \     ------->         \      ------->     /     /  \ 
     *           4    5                      3                 2     6    5     
     *            \                         /  \ 
     *             6                       6    5 
     *  
     *  
     * 情况C：当p的右孩子的左子树根节点的balance = EQUAL_HIGH 
     *          1                       1                           4 
     *         /  \                    /  \                        /   \ 
     *        2    3          右旋                   2    4           左旋                            1     3 
     *            /  \     ------->       /  \      ------->     / \    / \ 
     *           4    5                  6    3                 2   6  7   5     
     *          / \                          /  \ 
     *         6   7                        7    5 
     * */  
	private void rightBalance(Node p){
		Node rc=p.right,ld;
		switch(rc.balance){
			case RIGHT_HIGH:/**新结点插入到t的右孩子的右子树上，需要单左旋处理*/
				rc.balance=EQUAL_HIGH;
				p.balance=EQUAL_HIGH;
				/**对p进行左旋处理*/  
	            left_Rotate(p);  
				break;
		
			case LEFT_HIGH:/**新结点插入到t的右孩子的左子树上，需要双旋处理*/
				ld=rc.left;
				switch(ld.balance){
					case LEFT_HIGH:
						p.balance=EQUAL_HIGH;
						rc.balance=RIGHT_HIGH;
						break;
					case RIGHT_HIGH:
						p.balance=LEFT_HIGH;
						rc.balance=EQUAL_HIGH;
						break;
					case EQUAL_HIGH:
						p.balance=EQUAL_HIGH;
						rc.balance=EQUAL_HIGH;
						break;
				}
				ld.balance=EQUAL_HIGH;
				/**对p的右子树进行右旋处理*/
				right_Rotate(p.right);
				/**对p进行左旋处理*/
				left_Rotate(p);
				break;
		}
	
	}
	
	/** 
     * 左旋操作 
     *          p                                           r 
     *        /   \                                        /  \ 
     *       l     r                左旋操作                                                            p   rr 
     *           /   \          ----------->             / \    \ 
     *          rl    rr                                l   rl   rrr 
     *                 \ 
     *                  rrr 
     * */  
	private void left_Rotate(Node p){
		if(p!=null){
			Node r=p.right;/**获得p的右子树的根节点r*/
			
			p.right=r.left;/**将r的左子树转接到p的右子树上*/
			if(r.left!=null){/**如果r的左子树不为空，将左子树的父节点设置为p*/
				r.left.parent=p;
			}
			
			r.parent=p.parent;/**修改r的父节点，修改为p的父节点*/
			if(p.parent==null){/**如果p的父节点为null，那么现在r就是根节点了*/
				root=r;
			}else if(p==p.parent.left){/**如果p为其父节点的左孩子，将其父节点的左孩子指向r*/
				p.parent.left=r;
			}else if(p==p.parent.right){/**如果p为其父节点的右孩子，将其父节点的右孩子指向r*/
				p.parent.right=r;
			}
			
			r.left=p;/**将r的左孩子设置为p*/
			p.parent=r;/**将p的父节点设置为r*/
		}
	}
	
	
	/** 
     * 右旋操作 
     *  
     *               p                                    l 
     *             /  \              右旋操作                                                  /  \ 
     *            l    r        ------------->         ll   p 
     *           /  \                                /    /  \ 
     *          ll   lr                             lll  lr   r 
     *          / 
     *         lll 
     * */  
	private void right_Rotate(Node p){
	
		if(p!=null){
			Node l =p.left;/**获取p的左孩子l*/
			
			p.left=l.right;/**将l的右子树变为p的左子树*/
			if(l.right!=null){/**如果l的右子树不为空，将其父节点设置为p*/
				l.right.parent=p;
			}
			
			l.parent=p.parent;/**将r的父节点修改为p的父节点*/
			if(p.parent==null){/**如果p的父节点为null，即l为root*/
				root=l;
			}else if(p==p.parent.left){/**如果p为其父节点的左孩子，将p的父节点的左孩子指向l*/
				p.parent.left=l;
			}else if(p==p.parent.right){/**如果p为其父节点的右孩子，将p的父节点的右孩子指向l*/
				p.parent.right=l;
			}
			
			l.right=p;/**将l的右子树变为p*/
			p.parent=l;/**修改p的父节点为l*/
		}
	}
	
	
	public Node getRoot(){
		return this.root;
	}
	
	public static class Node{
		long key;
		/**结点的平衡因子*/
		byte balance=0;
		/**左孩子结点、右孩子结点、父节点*/
		Node left;
		Node right;
		Node parent;
		
		public Node(){}
		public Node(long key,Node parent){
			this.key=key;
			this.parent=parent;
		}
		
		public long getKey() {
			return key;
		}
		
		public Node getLeft() {
			return left;
		}
		public Node getRight() {
			return right;
		}
		//比较两个车牌的大小
		public int compareTo(long key) {
			
			if(this.key<key){
				return 1;
			}
			else if(this.key>key){
				return -1;
			}
			return 0;
		}
		
	}
}
