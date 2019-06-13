import java.util.Random;

public class Exercice3{
	
	class Base{
		
		private int nbLecteurs;
		private int nbEcrivainsEnAttente;
		private Boolean peutEcrire;
	
		//Conversion de la base de l'exercice 2 en moniteur.
		//peutEcrire est le booleen permettant l'acces ou non a l ecriture
		public Base() {
			this.nbLecteurs = 0;
			this.nbEcrivainsEnAttente = 0;
			this.peutEcrire = true;
		}
		
		//Relache un lecteur
		//Diminue le nombre de lecteurs. Peut continuer à interdire l'acces tant qu'il y a des lecteurs
		public void relacherLecteur() throws Exception{
			synchronized(this) {
				this.nbLecteurs--;
				if(this.nbLecteurs == 0) {
					this.peutEcrire = true;
					notify();
				} else {
					this.peutEcrire = false;
					notify();
				}
			}
		}

		//Dès qu'un processus veut lire, on interdit l'acces aux ecrivains
		public void acquerirLecteur() throws Exception{
			synchronized(this) {
			this.nbLecteurs++;
			this.peutEcrire = false;
			notify();
			}
		}

		//Acquerit un ecrivain. S'il y a deja un ecrivain dans la file, alors met en attente l'ecrivain suivant.
		//A condition qu'il n'y ait aucun lecteur dans la base.
		public void acquerirEcrivain() throws Exception{
			synchronized(this) {
				//S'il n'y a pas d'ecrivain, le processus prend la main et interdit aux autres processus d'ecrire
				if(this.peutEcrire) {
				this.peutEcrire = false;
				notify();
				} else {
					//Le processus attend s'il y a déjà un ecrivain qui a la main
					System.out.println("JE NE PEUX ECRIRE!");
					wait();
				}
			}	
		}

		//Relache l'écrivain courrant et permet à l'écrivain suivant de prendre la main
		public void relacherEcrivain() throws Exception{
			synchronized(this) {
				//Diminue le nombre d'ecrivains en attente et notifie tous les processus
				if(nbEcrivainsEnAttente > 0) {
				this.nbEcrivainsEnAttente--;
				notify();
				}
				//Tant qu'il y a des lecteurs, l'ecrivain est en pause.
				if(this.nbLecteurs != 0) {
					wait();
				} else {
					//S'il n'y a plus de lecteurs, alors l'ecrivain prend la main et notifie tous les processus
					this.peutEcrire = true;
					notify();
				}
			}
		}
	}

	class Processus extends Thread {
		public final Random rand = new Random();
		
		private Base base;
		private String nom;
		
		public Processus(Base b, String n) {
			this.base = b;
			this.nom = n;
		}
		
		public String getNom() {
			return this.nom;
		}

		@Override
		public void run() {
			while(true) {
				try { Thread.sleep(rand.nextInt(500)); }
				catch (Exception e) { e.printStackTrace(); }
				if (rand.nextBoolean()) {
					try {
						lire();
					}catch (Exception e) {
						e.printStackTrace();
					}
					} else {
					try {
					ecrire();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			if(this.getState() == State.WAITING) {System.out.println(this.getNom() + " est en pause");}
			}
		}

		//Le finally permet de mieux relacher le processus.
		//L'affichage permet de voir quels processus ont commence la lecture et quand elle se finit
		void lire() throws Exception{
			Boolean acquired = false;
			try {
				this.base.acquerirLecteur();
				System.out.println("Debut Lecture " + this.getNom());
				acquired = true;
				Thread.sleep(rand.nextInt(500));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(acquired) {this.base.relacherLecteur();
				System.out.println("Fin lecture " + this.getNom());
				}
			}
		}

		//Le finally permet de mieux relacher le processus.
		//L'affichage permet de voir quels processus ont commence a ecrire et quand il rend la main
		//A noter : Même si le processus veut ecrire, il ne pourra pas tant qu'il y a des lecteur ou un ecrivain arrive avant lui
		void ecrire() throws Exception {
			Boolean acquired = false;
			try {
				this.base.acquerirEcrivain();
				System.out.println("Debut Ecriture " + this.getNom());
				acquired = true;
				Thread.sleep(rand.nextInt(500));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(acquired) {this.base.relacherEcrivain();
				System.out.println("Fin Ecriture " + this.getNom());
				}
			}
		}
	}
	
	public static void main(String args[]) throws Exception {
		//Maniere crade de lancer l'execution du programme.
		Exercice3 f = new Exercice3();
		//Initialisation de la base
		Base b = f.new Base();
		//Initialisation des processus
		Processus t1 = f.new Processus(b, "P1");
		Processus t2 = f.new Processus(b, "P2");
		Processus t3 = f.new Processus(b, "P3");
		//Lancement des processus
		t1.start();
		t2.start();
		t3.start();
		//On join les 3 processus à la ressource
		t1.join();
		t2.join();
		t3.join();
	}
}