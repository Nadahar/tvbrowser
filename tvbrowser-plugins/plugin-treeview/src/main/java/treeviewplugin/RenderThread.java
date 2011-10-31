package treeviewplugin;

import java.util.concurrent.Semaphore;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

public class RenderThread extends Thread {
	private TreeViewPlugin plugin=null;
	private SearchEngine searchEngine=null;
	private JTree broadcastTree=null;
	private RootNode root=null;
	private Semaphore lock=new Semaphore(1,true);	
		
	private int requests=0;
	private boolean active=true;	
	
	private static int gid=0;
	//private int id=0;
	
	public RenderThread(TreeViewPlugin p,SearchEngine s,JTree t,RootNode r)throws Exception{
		super("TreeViewRenderThread "+(++gid));
		//id=gid;
		plugin=p;
		searchEngine=s;		
		broadcastTree=t;
		root=r;
		
		
	}
	protected void exit(){
		active=false;
		//System.out.println("[RenderThread.exit] id: "+id);
		lock.release();
		try{
			int i=0;
			
			while(isAlive()){
				Thread.sleep(100);
				i++;
				if(i>20){
					System.out.println("RenderThread exited due to timeout");
					break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void run(){			
		try{
			// Lock it until we need to do some work
			lock.acquire(); 
			while(active){
				
			// 	Lock thread for the next run
				//System.out.println("[RenderThread.exit] pre acquire lock "+requests+"id: "+id);
				lock.acquire();
				//System.out.println("[RenderThread.exit] post acquire lock "+requests+"id: "+id);
				if(!active)return;
				
				doWork();
							
				requests(-1);
				//System.out.println("run ended ");
			}	
		}catch(Exception e){
			e.printStackTrace();
		}			
	}
	
	synchronized protected void startWork(){
		//System.out.println("RenderThread.startWork] entered, prerequests: "+requests+"id: "+id);
		lock.release();
		requests(1); // TODO: Check if we need to swap both lines
	}
	
	synchronized private void requests(int c){
		requests+=c;
	}
	
	synchronized protected void doWork(){
		try{
			//System.out.println("[RenderThread.doWork] called id: "+id);
			if(requests>1){			
				//System.out.println("[RenderThread.doWork] canceled 1: "+id);
				return;
			}		
			
			searchEngine.resetSearchState();	
			if(requests>1){			
				//System.out.println("[RenderThread.doWork] canceled 2: "+id);
				return;
			}
			broadcastTree.setVisible(false);	
			plugin.refresh();
					
			DefaultTreeModel treeModel = new DefaultTreeModel(root);
					
			broadcastTree.setModel(treeModel);		
			
			treeModel.reload(root);		
			broadcastTree.setVisible(true);		
			Tools.isEnoughFreeMemory(plugin.log);
			//System.out.println("[RenderThread.doWork] left: "+id);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
