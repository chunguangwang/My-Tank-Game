import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


public class MyTankGame1 extends JFrame implements ActionListener{
	MyPanel mp=null;
	//define start panel
	MyStartPanel msp=null;
	//define menu
	JMenuBar jmb=null;
	
	JMenu jm1=null;
	//start the new game
	JMenuItem jmi1=null;
	//save the current game
	JMenuItem jmi2=null;
	//resume the last game
    JMenuItem jmi3=null;
    //exit the game
    JMenuItem jmi4=null;	
	
	
	public static void main(String[] args){
		MyTankGame1 mtg= new MyTankGame1();
	}
	
	//constructor
	public MyTankGame1(){
		
		//create menu and items
		jmb = new JMenuBar();
		jm1 = new JMenu("Game");
		jm1.setMnemonic('G');
		
		jmi1 = new JMenuItem("Start new game(N)");
		jmi1.setMnemonic('N');
		//response to jmi1
		jmi1.addActionListener(this);
		jmi1.setActionCommand("newgame");
		
		jmi2 = new JMenuItem("Save the game(S)");
		jmi2.setMnemonic('S');
		//response to jmi2
		jmi2.addActionListener(this);
		jmi2.setActionCommand("save");
		
		jmi3 = new JMenuItem("Resume last game(r)");
		jmi3.setMnemonic('R');
		//response to jmi3
		jmi3.addActionListener(this);
		jmi3.setActionCommand("resume");
		
		jmi4 = new JMenuItem("Exit the game(E)");
		jmi4.setMnemonic('E');
		//response to jmi4
		jmi4.addActionListener(this);
		jmi4.setActionCommand("exit");
		
		
		jm1.add(jmi1);
		jm1.add(jmi2);
		jm1.add(jmi3);
		jm1.add(jmi4);
		jmb.add(jm1);
		
		
		msp = new MyStartPanel();
		Thread tmsp=new Thread(msp);
		tmsp.start();
		
		this.setJMenuBar(jmb);
		this.add(msp);
		
		this.setSize(600,500);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent arg0){
		//response correspondingly to user's different clicks
		if(arg0.getActionCommand().equals("newgame")){
			if(mp !=null){
				this.remove(mp);
			}
			mp = new MyPanel("newgame");
			Thread t=new Thread(mp);
		    t.start();
			//remove the previous panel
			this.remove(msp);
			
			this.add(mp);
			this.addKeyListener(mp);
			//display refresh
			this.setVisible(true);
			
		}else if(arg0.getActionCommand().equals("exit")){
			//exit the game;
			Recorder.keepRecording();
			System.exit(0);
		}else if(arg0.getActionCommand().equals("save")&&mp != null){
			//save the current game state to file
			Recorder recorder=new Recorder();
			recorder.setEts(mp.emtl);
			recorder.setHr(mp.hero);
			recorder.keepRecAndEnemyTank();
			//exit
			System.exit(0);
		}else if(arg0.getActionCommand().equals("resume")){
			mp = new MyPanel("continue");
			Thread t=new Thread(mp);
		    t.start();
			//remove the previous panel
			this.remove(msp);
			this.add(mp);
			this.addKeyListener(mp);
			//display refresh
			this.setVisible(true);
		}
	}
}

//Hint panel
class MyStartPanel extends JPanel implements Runnable{
	boolean times=true;
	public void paint(Graphics g){
		super.paint(g);
		g.fillRect(0,0,400,300);
		//this.setBackground(Color.black);
		//hint information
		int style = Font.BOLD | Font.ITALIC;
		Font font = new Font ("Garamond", style , 20);
		if(times==true){
			g.setFont(font);
			g.setColor(Color.yellow);
			g.drawString("Stage: 1", 150, 150);
			times=false;
		}else{
			times=true;
		}
	}
	
	public void run(){
		
		while(true){
			//sleep
			try{
				Thread.sleep(750);
			}catch(Exception e){
				e.printStackTrace();
			}
			this.repaint();
		}
	}
}

//my panel
class MyPanel extends JPanel implements KeyListener,Runnable{
	int x=190;
	int y=270;
	int speed=1;
	boolean stop=false;
	public static int plwidth=400;
	public static int plheight=300;
	int dir=0;
	int enSize=3;
	
	// define 3 images
	Image img1=null;
	Image img2=null;
	Image img3=null;
	Image imgbu=null;
	Image imgbr=null;
	Image imgbd=null;
	Image imgbl=null;
	// define bomb list
	ArrayList<Bomb> bombs= new ArrayList<Bomb>();
	ArrayList<Bomb> bombsp= new ArrayList<Bomb>();
	
	// define tank
	Hero hero=null;
	
	//tell to resume the last game or start new game
	// define enemy tank list
	ArrayList<EnemyTank> emtl= new ArrayList<EnemyTank>();
	ArrayList<Node> nodes=new ArrayList<Node>();
	
	
	// constructor
	public MyPanel(String flag){
		//recover record
		Recorder.getRecording();
		
		if(flag.equals("newgame")){
		// initialize enemies' tanks
			for(int i=0;i<400;i=i+60){
				EnemyTank emt= new EnemyTank(i,0);
				emt.setDir(2);
				emtl.add(emt);
				Thread t= new Thread(emt);
				t.start();
			}
			// initialize my tank
		     hero=new Hero(x, y);
		}else{
			// initialize enemies' tanks
			nodes=Recorder.getNodes();
			
			EnemyTank emt=null;
			Shot shot=null;
			for(int i=0;i<nodes.size();i++){
				Node node=nodes.get(i);
				if(node.name.equals("en")){
					emt= new EnemyTank(node.x,node.y);
					emt.setDir(node.direct);
					emtl.add(emt);
					Thread t= new Thread(emt);
					t.start();
				}
				if(node.name.equals("eb")){
					shot=new Shot(node.x,node.y,node.direct,emt.speed*3);
					emt.ss.add(shot);
					Thread t= new Thread(shot);
					t.start();
				}
				if(node.name.equals("hr")){
					hero=new Hero(node.x,node.y);
					hero.setDir(node.direct);
				}
				if(node.name.equals("hb")){
					shot=new Shot(node.x,node.y,node.direct,hero.speed*3);
					hero.ss.add(shot);
					Thread t= new Thread(shot);
					t.start();
				}
			}
		}
		
	    // initialize images
		try {
			img1= ImageIO.read(new File("images/bullet_explosion_1.png"));
			img2= ImageIO.read(new File("images/bullet_explosion_2.png"));
			img3= ImageIO.read(new File("images/bullet_explosion_3.png"));
			imgbu= ImageIO.read(new File("images/bullet_up.png"));
			imgbr= ImageIO.read(new File("images/bullet_right.png"));
			imgbd= ImageIO.read(new File("images/bullet_down.png"));
			imgbl= ImageIO.read(new File("images/bullet_left.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//img1= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/images/bullet_explosion_1.png"));
		//img2= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/images/bullet_explosion_2.png"));
		//img3= Toolkit.getDefaultToolkit().getImage(Panel.class.getResource("/images/bullet_explosion_3.png"));
	}
	
	// if a tank is hit by a bullet
    public void hitTank(Shot s, EnemyTank emt){
		switch(emt.dir){
			case 0:
			case 2:
			       if(s.x>=emt.getX()&&(s.x<=emt.getX()+20)&&s.y>=emt.getY()&&(s.y<=emt.getY()+30))
				   {
					   //bullet dies
					   s.isLive1=false;
					   //enemy tank dies
					   emt.isLive=false;
					   // create a new bomb
					   Recorder.reduceEnNum();
					   Recorder.addEnNumRed();
					   Bomb bomb= new Bomb(emt.getX(),emt.getY());
					   bombs.add(bomb);
				   }  
			       break;
		    case 1:
			case 3:
			       if(s.x>=emt.getX()&&(s.x<=emt.getX()+30)&&s.y>=emt.getY()&&(s.y<=emt.getY()+20)){
					   //bullet dies
					   s.isLive1=false;
					   //enemy tank dies
					   emt.isLive=false;
					   Recorder.reduceEnNum();
					   Recorder.addEnNumRed();
					   Bomb bomb= new Bomb(emt.getX(),emt.getY());
					   bombs.add(bomb);
				   }
			       break;
		}
	}	
		
	
	// overwrite paint
	public void paint(Graphics g){
		super.paint(g);
		Bomb bomb=null;
		g.fillRect(0,0,plwidth,plheight);
		//draw enemy tanks
		for(int i=0;i<emtl.size();i++){
		  EnemyTank emt= emtl.get(i);
		  if(emt.isLive==true){
		  this.drawTank(emt.getX(), emt.getY(),g, emt.dir,0);
	    //draw enemy tanks' bullets
		  for(int j=0;j<emt.ss.size();j++){
			Shot st = emt.ss.get(j);
			if(st != null && st.isLive1==true&&st.isLive2==true){
				drawBullet(st.x,st.y, g, st.dir);
				//g.draw3DRect(st.x,st.y, 1,1,false);
			}
			if(st.isLive2==false){
				switch(st.dir){
				case 0:
						bomb= new Bomb(st.x-6,st.y-8);
						break;
				case 1:
						bomb= new Bomb(st.x-10,st.y-6);
						break;
				case 2:
						bomb= new Bomb(st.x-6,st.y-10);
						break;
				case 3:
						bomb= new Bomb(st.x-8,st.y-6);
						break;
				}
				 bombsp.add(bomb);
				emt.ss.remove(st);
				
			}
		}
		  }
	      else
			this.emtl.remove(emt);
		}
		
		// draw my tank
		if(hero !=null){
			this.drawTank(hero.getX(), hero.getY(), g, dir, 1);
		}
		
		// draw boms
		for(int i=0;i<this.bombs.size();i++){
			bomb=bombs.get(i);
			if(bomb.life>6){
				g.drawImage(img1,bomb.x,bomb.y,32,32,this);
			} else if(bomb.life>3){
				g.drawImage(img2,bomb.x,bomb.y,32,32,this);
			} else if(bomb.life>0){
				g.drawImage(img3,bomb.x,bomb.y,32,32,this);
			}
			bomb.lifeDown();
			if(bomb.life==0){
			  this.bombs.remove(bomb);	
			}
		}
		
		
		for(int i=0;i<this.bombsp.size();i++){
			bomb=bombsp.get(i);
			if(bomb.life>6){
				g.drawImage(img1,bomb.x,bomb.y,16,16,this);
			} else if(bomb.life>3){
				g.drawImage(img2,bomb.x,bomb.y,16,16,this);
			} else if(bomb.life>0){
				g.drawImage(img3,bomb.x,bomb.y,16,16,this);
			}
			bomb.lifeDown();
			if(bomb.life==0){
			  this.bombs.remove(bomb);	
			}
		}
		
		// draw my bullets
		if(this.hero !=null){
			for(int i=0;i<this.hero.ss.size();i++){
				Shot st = this.hero.ss.get(i);
				if(st != null && st.isLive1==true&&st.isLive2==true){
					drawBullet(st.x,st.y, g, st.dir);
					//g.draw3DRect(st.x,st.y, 1,1,false);
				}
				if(st.isLive2==false){
					switch(st.dir){
					case 0:
							bomb= new Bomb(st.x-6,st.y-8);
							break;
					case 1:
							bomb= new Bomb(st.x-10,st.y-6);
							break;
					case 2:
							bomb= new Bomb(st.x-6,st.y-10);
							break;
					case 3:
							bomb= new Bomb(st.x-8,st.y-6);
							break;
					}
					bombsp.add(bomb);
					this.hero.ss.remove(st);
				}
			}
		}
	   // draw hint information
	 this.showInfo(g);
		
	}
	
	
	
	//draw hint information
	public void showInfo(Graphics g){
		//draw hint information tank

		this.drawTank(100,320,g,0,0);
		g.setColor(Color.black);
		g.drawString(Recorder.getEnNum()+"",80,340);
		
		this.drawTank(160,320,g,0,1);
		g.setColor(Color.black);
		g.drawString(Recorder.getMyLife()+"",140,340);
		
		
		//draw player's accomplishment
		g.setColor(Color.black);
		int style = Font.BOLD | Font.ITALIC;
		Font font = new Font ("Garamond", style , 15);
		g.setFont(font);
		g.drawString("Grade",420,30);
		
		this.drawTank(420,60,g,0,0);
		
		g.setColor(Color.black);
		g.drawString(Recorder.getAllEnNum()+"", 455,80);
	}
	// instantialize abstract methods
	public void keyPressed(KeyEvent e){
		//System.out.println(e.getKeyChar());
		if(e.getKeyCode()==KeyEvent.VK_S&&stop==false){

			if((y+20)<plheight){
			y+=2*this.speed;
			dir=2;
			}
		}else if(e.getKeyCode()==KeyEvent.VK_W&&stop==false){
			if(y>0){
			y-=2*this.speed;
			dir=0;
			}
		}else if(e.getKeyCode()==KeyEvent.VK_A&&stop==false){
			if(x>0){
			x-=2*this.speed;
			dir=3;
			}
		}else if(e.getKeyCode()==KeyEvent.VK_D&&stop==false){
			if((x+20)<plwidth){
			x+=2*this.speed;
			dir=1;
			}
		}
		if(e.getKeyCode()==KeyEvent.VK_J&&stop==false){
			if(this.hero.ss.size()<=4)
			this.hero.fire();
		}
		if(e.getKeyCode()==KeyEvent.VK_SPACE){
			if(stop==false){
			this.speed=0;
			for(int i=0;i<emtl.size();i++){
				  EnemyTank emt= emtl.get(i);
				  emt.speed=0;
				  for(int j=0;j<emt.ss.size();j++){
					  Shot st=emt.ss.get(j);
					  st.speed=0;	
				  }
				  emt.timeInt=Integer.MAX_VALUE;
			}
			for(int i=0;i<this.hero.ss.size();i++){
				Shot st=this.hero.ss.get(i);
				st.speed=0;
			}
			stop=true;
			}else{
				this.speed=1;
			for(int i=0;i<emtl.size();i++){
				  EnemyTank emt= emtl.get(i);
				  emt.speed=1;
				  for(int j=0;j<emt.ss.size();j++){
					  Shot st=emt.ss.get(j);
					  st.speed=1;
				  }
				  emt.timeInt=400;
			}
			for(int i=0;i<this.hero.ss.size();i++){
				Shot st=this.hero.ss.get(i);
				st.speed=1;
			}
			stop=false;
		}
		}
		hero.setX(x);
		hero.setY(y);
		hero.setDir(dir);
		this.repaint();
	}
	public void keyReleased(KeyEvent arg0){
		
	}
	public void keyTyped(KeyEvent arg0){
		
	}
	
	
	public boolean touchTank(EnemyTank emt){
		for(int i=0;i<emtl.size();i++){
			EnemyTank et= emtl.get(i);
			if(emt != et){
				switch(emt.dir){
					case 0:
					        //my tank up
							if(et.dir==0||et.dir==2)
							{
								if(emt.x>=et.x&&emt.x<=et.x+20&&emt.y>=et.y&&emt.y<=et.y+30){
									return true;
								}
								if(emt.x+20>=et.x&&emt.x+20<=et.x+20&&emt.y>=et.y&&emt.y<=et.y+30){
									return true;
								}
							}
							if(et.dir==1||et.dir==3)
							{
								if(emt.x>=et.x&&emt.x<=et.x+30&&emt.y>=et.y&&emt.y<=et.y+20){
									return true;
								}
								if(emt.x+20>=et.x&&emt.x+20<=et.x+20&&emt.y>=et.y&&emt.y<=et.y+20){
									return true;
								}
							}
							break;
					case 1:
					      //my tank right
					      if(et.dir==0||et.dir==2)
							{
								if(emt.x+30>=et.x&&emt.x+30<=et.x+20&&emt.y>=et.y&&emt.y<=et.y+30){
									return true;
								}
								if(emt.x+30>=et.x&&emt.x+30<=et.x+20&&emt.y+20>=et.y&&emt.y+20<=et.y+30){
									return true;
								}
							}
							if(et.dir==1||et.dir==3)
							{
								if(emt.x+30>=et.x&&emt.x+30<=et.x+30&&emt.y>=et.y&&emt.y<=et.y+20){
									return true;
								}
								if(emt.x+30>=et.x&&emt.x+30<=et.x+20&&emt.y+20>=et.y&&emt.y+20<=et.y+20){
									return true;
								}
							}
					      break;
				    case 2:
							//my tank down
							if(et.dir==0||et.dir==2)
							{
								if(emt.x>=et.x&&emt.x<=et.x+20&&emt.y+30>=et.y&&emt.y+30<=et.y+30){
									return true;
								}
								if(emt.x+20>=et.x&&emt.x+20<=et.x+20&&emt.y+30>=et.y&&emt.y+30<=et.y+30){
									return true;
								}
							}
							if(et.dir==1||et.dir==3)
							{
								if(emt.x>=et.x&&emt.x<=et.x+30&&emt.y+30>=et.y&&emt.y+30<=et.y+20){
									return true;
								}
								if(emt.x+20>=et.x&&emt.x+20<=et.x+20&&emt.y+30>=et.y&&emt.y+30<=et.y+20){
									return true;
								}
							}
							break;
					case 3:
							//my tank left
					      if(et.dir==0||et.dir==2)
							{
								if(emt.x>=et.x&&emt.x<=et.x+20&&emt.y>=et.y&&emt.y<=et.y+30){
									return true;
								}
								if(emt.x>=et.x&&emt.x<=et.x+20&&emt.y+20>=et.y&&emt.y+20<=et.y+30){
									return true;
								}
							}
							if(et.dir==1||et.dir==3)
							{
								if(emt.x>=et.x&&emt.x<=et.x+30&&emt.y>=et.y&&emt.y<=et.y+20){
									return true;
								}
								if(emt.x>=et.x&&emt.x<=et.x+20&&emt.y>=et.y+20&&emt.y<=et.y+20){
									return true;
								}
							}
							break;
					
				
				}
			}
		}
		return false;
	}
	
	//draw bullet
	public void drawBullet(int x, int y, Graphics g, int direct){
		//direction
		switch(direct){
			case 0:
					g.drawImage(imgbu,x-2,y+2,4,4,this);
					break;
			case 1:
					g.drawImage(imgbr,x+2,y-2,4,4,this);
					break;
			case 2:
					g.drawImage(imgbd,x-2,y-2,4,4,this);
					break;
			case 3:
					g.drawImage(imgbl,x-2,y-2,4,4,this);
					break;
		}
	}
	
	//draw tank
	public void drawTank(int x, int y, Graphics g, int direct, int type){
		//type
		switch(type){
			case 0: 
			  g.setColor(Color.cyan);
			  break;
			 case 1:
			  g.setColor(Color.yellow);
			  break;
		}
		
		//direct
		switch(direct){
			case 0:
			 
		      //draw my tank
		      // draw left rectangle
		      g.fill3DRect(x, y,5,30,true);
		      //draw right rectangle
		      g.fill3DRect(x+15, y, 5,30,true);
		      //draw center rectangle
		      g.fill3DRect(x+5, y+5, 10,20,true);
		
		      //draw circle
		       g.setColor(Color.GREEN);
		       g.fillOval(x+5,y+10,10,10);
		        //draw the line
		       g.drawLine(x+10, y+15,x+10,y);
			   break;
			   
			   case 1:
			 
		      //draw my tank
		      // draw left rectangle
		      g.fill3DRect(x, y,30,5,true);
		      //draw right rectangle
		      g.fill3DRect(x, y+15, 30,5,true);
		      //draw center rectangle
		      g.fill3DRect(x+5, y+5, 20,10,true);
		
		      //draw circle
		       g.setColor(Color.GREEN);
		       g.fillOval(x+10,y+5,10,10);
		        //draw the line
		       g.drawLine(x+15, y+10,x+30,y+10);
			   break;
			   
			   
			   case 2:
			 
		      //draw my tank
		      // draw left rectangle
		      g.fill3DRect(x, y,5,30,true);
		      //draw right rectangle
		      g.fill3DRect(x+15, y, 5,30,true);
		      //draw center rectangle
		      g.fill3DRect(x+5, y+5, 10,20,true);
		
		      //draw circle
		       g.setColor(Color.GREEN);
		       g.fillOval(x+5,y+10,10,10);
		        //draw the line
		       g.drawLine(x+10, y+15,x+10,y+30);
			   break;
			   
			   
			    case 3:
			 
		      //draw my tank
		      // draw left rectangle
		      g.fill3DRect(x, y,30,5,true);
		      //draw right rectangle
		      g.fill3DRect(x, y+15, 30,5,true);
		      //draw center rectangle
		      g.fill3DRect(x+5, y+5, 20,10,true);
		
		      //draw circle
		       g.setColor(Color.GREEN);
		       g.fillOval(x+10,y+5,10,10);
		        //draw the line
		       g.drawLine(x, y+10,x+15,y+10);
			   break;
		}
	}
	
	public void run(){
		//every 100ms repaint
		while(true){
			try{
				Thread.sleep(100);
			} catch(Exception e){
				e.printStackTrace();
			}
		// if an enemy tank is hit by one of my bullets
		for(int i=0;i<emtl.size();i++){
			EnemyTank emt= emtl.get(i);
			for(int j=0;j<this.hero.ss.size();j++)
			{
				Shot sb=this.hero.ss.get(j);
				if(sb.isLive1==true&&sb.isLive2==true)
				hitTank(sb,emt);
			}
			
			
			
			
	//enemy tanks moving
		if(emt.dir==2){
			if((emt.y+20)<plheight && (!touchTank(emt))){
			emt.y+=2*this.speed;
			
			}else{
				emt.dir=(int)(Math.random()*4);
			}
		}else if(emt.dir==0){
			if(emt.y>0&& (!touchTank(emt))){
			emt.y-=2*this.speed;
			
			}else{
				emt.dir=(int)(Math.random()*4);
			}
		}else if(emt.dir==3){
			if(emt.x>0&& (!touchTank(emt))){
			emt.x-=2*this.speed;
			
			}else{
				emt.dir=(int)(Math.random()*4);
			}
		}else if(emt.dir==1){
			if((emt.x+20)<plwidth&& (!touchTank(emt))){
			emt.x+=2*this.speed;
			
			}else{
				emt.dir=(int)(Math.random()*4);
			}
		}
			
		}
			this.repaint();
		}
	}
}

class Node{
	String name;
	int x;
	int y;
	int direct;
	
	public Node(String type, int x, int y, int direct){
		this.name=type;
		this.x=x;
		this.y=y;
		this.direct=direct;
	}
}

//recording class
class Recorder{
	// record how many enemies every stage
	private static int enNum=20;
	// my life
	private static int myLife=3;
	// how many enemies eliminated
	private static int allEnNum=0;
	
	//recover nodes from file
	static ArrayList<Node> nodes=new ArrayList<Node>();
	
	private static FileWriter fw=null;
	private static BufferedWriter bw=null;
	private static FileReader fr=null;
	private static BufferedReader br=null;
	private ArrayList<EnemyTank> ets=new ArrayList<EnemyTank>();
	private  Hero hr=null;
	
	//read from file
	public static ArrayList<Node> getNodes(){
		try{
			fr=new FileReader("game.txt");
			br=new BufferedReader(fr);
			//read first line
			String n=br.readLine();
			allEnNum=Integer.parseInt(n);
			String[] xyz=new String[4];
			n=br.readLine();
			while(n!=null){
				xyz=n.split(" ");
				Node node=new Node(xyz[0],Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2]),Integer.parseInt(xyz[3]));
				nodes.add(node);
				n=br.readLine();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				br.close();
				fr.close();
			}catch(Exception e){
				
			}
		}
		return nodes;
	}
	
	//read record from file
	public static void getRecording(){
		try{
			fr=new FileReader("game.txt");
			br=new BufferedReader(fr);
			String n=br.readLine();
			allEnNum=Integer.parseInt(n);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				br.close();
				fr.close();
			}catch(Exception e){
				
			}
		}
	}
	// save enemy tanks' coordinates, directions and allEnNum
	
	public void keepRecAndEnemyTank(){
		try{
				
				fw=new FileWriter("game.txt");
				bw=new BufferedWriter(fw);
				bw.write(allEnNum+"\r\n");
				for(int i=0;i<ets.size();i++){
					EnemyTank emt=ets.get(i);
					System.out.println("New land");
					if(emt.isLive){
						bw.write("en"+" "+emt.getX() + " " + emt.getY() + " " + emt.dir+"\r\n");
						for(int j=0;j<emt.ss.size();j++){
							Shot st=emt.ss.get(j);
							bw.write("eb" +" "+ st.x + " " + st.y + " " + st.dir+"\r\n");
						}
					}
				}
				bw.write("hr"+" "+hr.x+" "+hr.y+" "+ hr.dir+"\r\n");
				for(int j=0;j<hr.ss.size();j++){
						Shot st=hr.ss.get(j);
						bw.write("hb"+" "+st.x + " " + st.y + " " + st.dir+"\r\n");
					}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					bw.close();
					fw.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
	}
	
	//save current game state information
	public static void keepRecording(){
		   
			
			try{
				System.out.println("New land");
				fw=new FileWriter("game.txt");
				bw=new BufferedWriter(fw);
				bw.write(allEnNum+"\r\n");
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					bw.close();
					fw.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
	}
	public static int getEnNum(){
		return enNum;
	}
	public static void setEnNum(int n){
		enNum=n;
	}
	public static int getMyLife(){
		return myLife;
	}
	public static void setMyLife(int n){
		myLife=n;
	}
	public static int getAllEnNum(){
		return allEnNum;
	}
	public static void setAllEnNum(int n){
		allEnNum=n;
	}
	
	//reduce enemy number
	public static void reduceEnNum(){
		enNum--;
	}
	
	//record add
	public static void addEnNumRed(){
		allEnNum++;
	}
	
	public void setEts(ArrayList<EnemyTank> etl){
		ets=etl;
	}
	public void setHr(Hero hero){
		hr=hero;
	}
}



class Bomb {
	int x;
	int y;
	int life=9;
	boolean isLive=true;
	public Bomb(int x, int y){
		this.x=x;
		this.y=y;
	}
	public void lifeDown(){
		if(life>0){
		life--;
		}else{
		this.isLive=false;
		}
	}
}


class Shot implements Runnable{
	int x;
	int y;
	int dir;
	int speed;
	
	// if living
	boolean isLive1=true;
	boolean isLive2=true;
	public Shot(int x, int y, int dir, int speed){
		this.x=x;
		this.y=y;
		this.dir = dir;
		this.speed = speed;
	}
	public void run(){
		
		while(true){
			
			try{
				Thread.sleep(50);
			} catch (Exception e){
				
			}
			switch(dir){
				case 0:
					//up
					y-=speed;
					break;
				case 1:
				    //right
					x+=speed;
					break;
				case 2:
				    //down
					y+=speed;
					break;
				case 3:
				     //left
					x-=speed;
					break;
				     
			}
			//when bullet dies
			if(x<0||x>MyPanel.plwidth||y<0||y>MyPanel.plheight){
				this.isLive2=false;
				break;
			}
		}
	}
}



class Tank{
	int color;
	int speed=1;
	int dir;
	boolean isLive=true;
	//Shot
	Vector<Shot> ss=new Vector<Shot>();
	Shot s=null;
	// tank's horizontal coordinate
	
	int x=0;
	// tank's vertical coordinate
	int y=0;
	public Tank(int x, int y){
		this.x=x;
		this.y=y;
	}
	
	public int getX(){
		return x;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public int getY(){
		return y;
	}
	
	public void setY(int y){
		this.y=y;
	}
	
	public void setSpeed(int speed)
	{
		this.speed = speed;
	}
	public int getSpeed(){
		return speed;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setDir(int dir){
		this.dir=dir;
	}
	public int getDir(int dir){
		return dir;
	}
	
	//fire
	public void fire(){
		switch(this.dir){
			case 0:
				s=new Shot(x+10,y,0,speed * 3);
				break;
			case 1:
			    s=new Shot(x+30,y+10,1,speed * 3);
				break;
			case 2:
			    s=new Shot(x+10,y+30,2, speed * 3);
				break; 
			case 3:
				s=new Shot(x,y+10,3, speed * 3);
				break;
		}
		ss.add(s);
		//start bullet thread
		Thread t= new Thread(s);
		t.start();
	}
}


// enemy's tank
class EnemyTank extends Tank implements Runnable{
    int timeInt=400;
	public EnemyTank(int x, int y){
		super(x,y);
	}
	int time=0;
	
	public void run(){
		while(true){
			try{
				Thread.sleep(timeInt);
			}catch(Exception e){
				e.printStackTrace();
			}
			time+=timeInt;
			if(time/timeInt==3){
			if(this.ss.size()<=4){
			  this.fire();	
			}
			}
			if(time%(10*timeInt)==0){
			  time=0;
			  this.dir=(int)(Math.random()*4);
			}
		}
	}
	
}



//my tank
class Hero extends Tank{
	
	public Hero(int x, int y)
	{
		super(x,y);
	}
	
	
}