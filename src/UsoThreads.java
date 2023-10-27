import java.awt.geom.*;

import javax.swing.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class UsoThreads {
		
	public static void main(String[] args) {


	
		
		JFrame marco=new MarcoRebote();
		
		marco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		marco.setSize(1000, 500);
		marco.setVisible(true);

	}

}

//Movimiento de la pelota-----------------------------------------------------------------------------------------

class Pelota{
	// Mueve la pelota invirtiendo posición si choca con límites
	public void mueve_pelota(Rectangle2D limites){
		
		x+=dx;
		
		y+=dy;
		
		if(x<limites.getMinX()){
			
			x=limites.getMinX();
			
			dx=-dx;
		}
		
		if(x + TAMX>=limites.getMaxX()){
			
			x=limites.getMaxX() - TAMX;
			
			dx=-dx;
		}
		
		if(y<limites.getMinY()){
			
			y=limites.getMinY();
			
			dy=-dy;
		}
		
		if(y + TAMY>=limites.getMaxY()){
			
			y=limites.getMaxY()-TAMY;
			
			dy=-dy;
			
		}
		
	}
	
	//Forma de la pelota en su posición inicial
	public Ellipse2D getShape(){
		return new Ellipse2D.Double(x,y,TAMX,TAMY);
	}	

	public void suspenderPelota(){
		suspendida = true;
	}

	public void reanudarPelota(){
		suspendida = false;
		synchronized (this){
			notify();
		}
	}

	public boolean estaSuspendida(){
		return suspendida;
	}

	public void esperar() throws InterruptedException  {
		synchronized (this){
			while(suspendida){
				wait();
			}
		}
	}

	public void dormirPelota(int tiempo){
		if(!estaSuspendida()){		
			suspenderPelota();
			new Thread(()->{
			try {
				Thread.sleep(tiempo);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			reanudarPelota();
			}).start();
	
		}

	}
	
	private static final int TAMX=15;
	
	private static final int TAMY=15;
	
	private double x=0;
	
	private double y=0;
	
	private double dx=1;
	
	private double dy=1;

	private boolean suspendida = false;

	
	
}

// Lámina que dibuja las pelotas----------------------------------------------------------------------

class LaminaPelota extends JPanel{
	
	//Añadimos pelota a la lámina
	
	public void add(Pelota b){
		
		pelotas.add(b);
	}
	
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		Graphics2D g2=(Graphics2D)g;
		
		for(Pelota b: pelotas){
			
			g2.fill(b.getShape());
		}
		
	}

	public ArrayList<Pelota> getPelotas(){
		return pelotas;
	}

	public void limpiarPantalla(){
		pelotas.clear();
		repaint();
	}


	private ArrayList<Pelota> pelotas=new ArrayList<Pelota>();
}


//Marco con lámina y botones------------------------------------------------------------------------------

class MarcoRebote extends JFrame{

	
	public MarcoRebote(){
		
		setBounds(600,300,400,350);
		
		setTitle ("Rebotes");
		
		lamina=new LaminaPelota();
		
		add(lamina, BorderLayout.CENTER);
		
		JPanel laminaBotones=new JPanel();
		
		ponerBoton(laminaBotones, "Dale!", new ActionListener(){
			
			public void actionPerformed(ActionEvent evento){
				
				comienza_el_juego();
			}
			
		});
		
		
		ponerBoton(laminaBotones, "Salir", new ActionListener(){
			
			public void actionPerformed(ActionEvent evento){
				
				System.exit(0);
				
			}
			
		});
		ponerBoton(laminaBotones, "Suspender Pelota", new ActionListener() {
			public void actionPerformed(ActionEvent evento){
				for(Pelota pelota : lamina.getPelotas()){
					if(!pelota.estaSuspendida()){
						pelota.suspenderPelota();
						break;
					}
				}
				
			}
		});
		ponerBoton(laminaBotones, "Suspender Todas", new ActionListener() {
			public void actionPerformed(ActionEvent evento){
				for(Pelota pelota : lamina.getPelotas()){
					if(!pelota.estaSuspendida()){
						pelota.suspenderPelota();
					}
				}
				
			}
		});

		ponerBoton(laminaBotones, "Reanudar Pelota", new ActionListener() {
			public void actionPerformed(ActionEvent evento){
				for(Pelota pelota : lamina.getPelotas()){
					if(pelota.estaSuspendida()){
						pelota.reanudarPelota();
						break;
					}
				}
			}
		});

		ponerBoton(laminaBotones, "Reanudar Todas", new ActionListener() {
			public void actionPerformed(ActionEvent evento){
				for(Pelota pelota : lamina.getPelotas()){
					if(pelota.estaSuspendida()){
						pelota.reanudarPelota();
					}
				}
			}
		});


		ponerBoton(laminaBotones, "Dormir Pelota", new ActionListener(){
			public void actionPerformed(ActionEvent evento ){
				for(Pelota pelota : lamina.getPelotas()){
					if(!pelota.estaSuspendida()){
					pelota.dormirPelota(2000);
					break;
				}
			}
			}
		});
		ponerBoton(laminaBotones, "Dormir Todas", new ActionListener() {
			public void actionPerformed(ActionEvent evento ){
				for(Pelota pelota : lamina.getPelotas()){
					pelota.dormirPelota(2000);
				}
			}
		});

		ponerBoton(laminaBotones, "Limpiar Pantalla", new ActionListener() {
    		public void actionPerformed(ActionEvent evento) {
        		lamina.limpiarPantalla();
    }
});



		
		add(laminaBotones, BorderLayout.SOUTH);
	}
	
	
	//Ponemos botones
	
	public void ponerBoton(Container c, String titulo, ActionListener oyente){
		
		JButton boton=new JButton(titulo);
		
		c.add(boton);
		
		boton.addActionListener(oyente);
		
	}
	
	//Añade pelota
	
	public void comienza_el_juego (){
		
					
			Pelota pelota=new Pelota();
			
			lamina.add(pelota);
			
			Runnable r = new PelotaThread(pelota, lamina);
			Thread t = new Thread(r);
			t.start();
			
	}

	private LaminaPelota lamina;
	
	class PelotaThread implements Runnable {
		private Pelota pelota;
		private LaminaPelota lamina;
		
		PelotaThread (Pelota p, LaminaPelota l) {
			pelota = p;
			lamina = l;
			
		}		

		@Override
		public void run() {

			for (int i = 1; i <= 3000; i++) {
			try{
				pelota.esperar();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
					pelota.mueve_pelota(lamina.getBounds());
					lamina.paint(lamina.getGraphics());
					
				try {
					Thread.sleep(4);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}	
		}	
	}
}