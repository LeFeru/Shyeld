import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

public class Principale {
	public static java.util.Scanner scanner = new java.util.Scanner(System.in);
	private Connection conn = null;
	private String user = "mmzough15"; // Seulement pour les tests
	private String password = "B6AFuY6"; // Seulement pour les tests
	private HashMap<String, PreparedStatement> listeSelects;
	private HashMap<String, PreparedStatement> listeProcedures;
	private HashMap<String, String> listeErreurs;

	public Principale() {
		connexion();
		setSelects();
		setProcedures();
		setErreurs();
	}

	private void connexion() {
		System.out.println("Initialisation de la connexion:");
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver PostgreSQL OK !");
			String url = "jdbc:postgresql://172.24.2.6:5432/dbrasli15?user=" + user+ "&password=" + password;
			//String url = "jdbc:postgresql://localhost:5434/projet?user=" + user
					//+ "&password=" + password;
			try {
				conn = DriverManager.getConnection(url);
				System.out.println("Connecté au serveur ! Avec "
						+ conn.getMetaData().getDriverName() + " "
						+ conn.getMetaData().getDriverVersion() + "{ "
						+ conn.getMetaData().getDriverMajorVersion() + ","
						+ conn.getMetaData().getDriverMinorVersion() + " }"
						+ " to " + conn.getMetaData().getDatabaseProductName()
						+ " " + conn.getMetaData().getDatabaseProductVersion()
						+ "\n");
				System.out.println("Read-only: " + conn.isReadOnly());
			} catch (SQLException e) {
				System.out.println("Impossible de joindre le server !");
				System.exit(1);
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Driver PostgreSQL manquant !");
			System.exit(1);
		}
	}

	public void deconnexion() {
		try {
			conn.close();
			System.out.println("Déconnecté du serveur avec succès !");
		} catch (SQLException e) {
			System.out.println("Echec lors de la déconnection");
			System.exit(1);
		}
	}

	private void setSelects() {
		try {
			listeSelects = new HashMap<String, PreparedStatement>();
			listeSelects.put("agents",
					conn.prepareStatement("SELECT a.* FROM projet.agents a"));
			listeSelects.put("combats",
					conn.prepareStatement("SELECT c.* FROM projet.combats c"));
			listeSelects
					.put("participations",
							conn.prepareStatement("SELECT p.* FROM projet.participations p"));
			listeSelects
					.put("reperages",
							conn.prepareStatement("SELECT r.* FROM projet.reperages r"));
			listeSelects
					.put("super_heros",
							conn.prepareStatement("SELECT sh.* FROM projet.super_heros sh"));
			listeSelects
					.put("agent",
							conn.prepareStatement("SELECT a.* FROM projet.agents a WHERE a.id_agent = ?"));
			listeSelects
					.put("combat",
							conn.prepareStatement("SELECT c.* FROM projet.combats c WHERE c.id_combat = ?"));
			listeSelects
					.put("reperage",
							conn.prepareStatement("SELECT r.* FROM projet.reperages r WHERE r.id_reperage = ?"));
			listeSelects
					.put("superheros",
							conn.prepareStatement("SELECT sh.* FROM projet.super_heros sh WHERE sh.id_sh = ?"));
			listeSelects
					.put("combats/agent",
							conn.prepareStatement("SELECT c.* FROM projet.combats c WHERE c.id_agent = ?"));
			listeSelects
					.put("getIdAgent",
							conn.prepareStatement("SELECT a.id_agent FROM projet.agents a WHERE a.login = ?"));
			listeSelects
					.put("superherosDisparus",
							conn.prepareStatement("SELECT * FROM projet.superherosDisparus;"));
			listeSelects.put("avertissement", conn
					.prepareStatement("SELECT * FROM projet.avertissement;"));
			listeSelects
					.put("classementSh",
							conn.prepareStatement("SELECT * FROM projet.classementSh;"));
			listeSelects
					.put("classementShVictoires",
							conn.prepareStatement("SELECT * FROM projet.classementShVictoires;"));
			listeSelects
					.put("classementShDefaites",
							conn.prepareStatement("SELECT * FROM projet.classementShDefaites;"));
			listeSelects
					.put("classementAgents",
							conn.prepareStatement("SELECT * FROM projet.classementAgents;"));
		} catch (SQLException e) {
			System.out
					.println("Un ou plusieurs prepareStatement a renvoyé une SQLException");
			System.exit(1);
		}
	}

	private void setProcedures() {
		listeProcedures = new HashMap<String, PreparedStatement>();
		try {
			listeProcedures
					.put("ajouterAgent",
							conn.prepareStatement("SELECT * FROM projet.ajouterAgent(?,?,?,?,?);"));
			listeProcedures.put("getIdAgent", conn
					.prepareStatement("SELECT * FROM projet.getIdAgent(?);"));
			listeProcedures
					.put("ajouterSH",
							conn.prepareStatement("SELECT * FROM projet.ajouterSH(?, ?, ?, ?);"));
			listeProcedures
					.put("ajouterReperage",
							conn.prepareStatement("SELECT * FROM projet.ajouterReperage(?, ?, ?, ?);"));
			listeProcedures
					.put("ajouterCombat",
							conn.prepareStatement("SELECT * FROM projet.ajouterCombat(?, ?, ?);"));
			listeProcedures
					.put("ajouterParticipation",
							conn.prepareStatement("SELECT * FROM projet.ajouterParticipation(?, ?, ?);"));
			listeProcedures
					.put("supprimerCombat",
							conn.prepareStatement("SELECT * FROM projet.supprimerCombat(?);"));
			listeProcedures.put("getIdSh",
					conn.prepareStatement("SELECT * FROM projet.getIdSh(?);"));
			listeProcedures
					.put("supprimerAgent",
							conn.prepareStatement("SELECT * FROM projet.supprimerAgent(?);"));
			listeProcedures
					.put("historiqueCombatsAgent",
							conn.prepareStatement("SELECT * FROM projet.historiqueCombatsAgent(?,?);"));
			listeProcedures
					.put("historiqueReperagesAgent",
							conn.prepareStatement("SELECT * FROM projet.historiqueReperagesAgent(?,?,?);"));
			listeProcedures
			.put("supprimerSh",
					conn.prepareStatement("SELECT * FROM projet.supprimerSh(?);"));

		} catch (SQLException e) {
			System.out
					.println("Un ou plusieurs prepareStatement a renvoyé une SQLException");
			System.exit(1);
		}
	}

	private void setErreurs() {
		listeErreurs = new HashMap<String, String>();
		listeErreurs.put("null value in column \"id_sh\"",
				"Le super-héros n'existe pas");
		listeErreurs
				.put("Pk_Agent",
						"L'id de l'agent doit être un entier strictement positif et unique");
		listeErreurs
				.put("Nom_Agent",
						"Le nom de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs
				.put("Prenom_Agent",
						"Le prenom de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs
				.put("Login_Agent",
						"Le login de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s), . , - , _");
		listeErreurs
				.put("Mdp_Agent",
						"Le mot de passe de l'agent ne peut être vide ou ne contenir que des espaces");
		listeErreurs
				.put("Salt_Agent",
						"Le Salt (key) de l'agent ne peut être vide ou ne contenir que des espaces");
		listeErreurs
				.put("Pk_Sh",
						"L'id du super-héros doit être un entier strictement positif et unique");
		listeErreurs
				.put("NomSh_Sh",
						"Le nom du super-héros doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s), . , - , _");
		listeErreurs
				.put("NomCivil_Sh",
						"Le nom civil du super-héros doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs
				.put("AdressePrivee_Sh",
						"L'adresse privée du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs
				.put("Origine_Sh",
						"L'origine du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs
				.put("TypePouvoir_Sh",
						"Le type de pouvoir du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs
				.put("PuissancePouvoir_Sh",
						"La puissance du pouvoir du super-héros doit être un entier strictement positif");
		listeErreurs
				.put("Faction_Sh",
						"La faction du super-héros doit être 'M' (pour Marvelle) ou 'D' (pour Décé Comics)");
		listeErreurs
				.put("DerniereX_Sh",
						"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("DerniereY_Sh",
						"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("DerniereDate_Sh",
						"La date du dernier repérage ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs
				.put("Pk_Reperage",
						"L'id du repérage doit être un entier strictement positif et unique");
		listeErreurs
				.put("Fk_Agent_Reperage",
						"La clé étrangère de l'agent doit faire référence à une clé primaire existante dans la table agent");
		listeErreurs
				.put("Fk_Sh_Reperage ",
						"La clé étrangère du super-héros doit faire référence à une clé primaire existante dans la table super_heros");
		listeErreurs
				.put("DateReperage_Reperage",
						"La date du repérage ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs
				.put("X_Reperage",
						"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("Y_Reperage",
						"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("Pk_Combat",
						"L'id du combat doit être un entier strictement positif et unique");
		listeErreurs
				.put("Fk_Agent_Combat",
						"La clé étrangère de l'agent doit faire référence à une clé primaire existante dans la table agent");
		listeErreurs
				.put("X_Combat",
						"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("Y_Combat",
						"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs
				.put("DateCombat_Combat",
						"La date ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs
				.put("Pk_Fk_Combat_Participation",
						"La clé étrangère du combat doit faire référence à une clé primaire existante dans la table combat");
		listeErreurs
				.put("Pk_Fk_Sh_Participation",
						"La clé étrangère du super-héros doit faire référence à une clé primaire existante dans la table super_heros");
		listeErreurs
				.put("Etat_Participation",
						"L'état d'une participation à un combat est soit: G pour gagner, P pour perdu et N pour nul");
	}

	private static void menu() {
		System.out.println("-	Quitter le SHYELD	(0)");
		System.out.println("-	Enregistrer un nouvel Agent (1)");
		System.out.println("-	Faire disparaître un Agent du SHYELD (2)");
		System.out.println("-	Voir les super-héros disparus (3)");
		System.out.println("-	Fonction d'avertissement (4)");
		System.out
				.println("-	Historique des combats(5)");
		System.out
				.println("-	Historique des reperages signalés par un Agent (6)");
		System.out.println("-	Classement des super-héros (7)");
		System.out
				.println("-	Classement des super-héros avec le plus de victoires (8)");
		System.out
				.println("-	Classement des super-héros avec le plus de défaites (9)");
		System.out.println("-	Classement des Agents (10)");
		System.out.println("-	Supprimer un super-héros (11)");
	}

	private void enregistrerAgent() {
		System.out.println("-	Entrez le nom du nouvel agent:");
		String nom = scanner.nextLine();
		System.out.println("-	Entrez le prénom du nouvel agent:");
		String prenom = scanner.nextLine();
		System.out.println("-	Entrez le login du nouvel agent:");
		String login = scanner.nextLine();
		System.out.println("-	Entrez le password du nouvel agent: ");
		String mdp = scanner.nextLine();
		String salt = BCrypt.gensalt();
		String mdpHashed = BCrypt.hashpw(mdp, salt);
		PreparedStatement procedure = listeProcedures.get("ajouterAgent");
		try {
			procedure.setString(1, nom);
			procedure.setString(2, prenom);
			procedure.setString(3, login);
			procedure.setString(4, mdpHashed);
			procedure.setString(5, salt);
			procedure.executeQuery();
			System.out.println("Bravo, un de plus !");
		} catch (SQLException e) {
			System.out.println("Ce login existe déjà");
		}
	}

	private void voirHistoriqueCombatsAgent() {
		PreparedStatement procedureHistorique = listeProcedures
				.get("historiqueCombatsAgent");
		String date1;
		String date2;
		try {
			System.out.println("Entrez la date de la première intervalle:");
			date1 = scanner.nextLine();
			System.out.println("Entrez la date de la seconde intervalle:");
			date2 = scanner.nextLine();
			procedureHistorique.setString(1, date1);
			procedureHistorique.setString(2, date2);
			ResultSet res = procedureHistorique.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.out.println("Date(s) invalide(s) ! !");
		}
	}

	private void voirHistoriqueReperagesAgent() {
		PreparedStatement procedureHistorique = listeProcedures
				.get("historiqueReperagesAgent");
		String date1;
		String date2;
		String login;
		try {
			System.out.println("-	Entrez le login de l'agent:");
			login = scanner.nextLine();
			System.out.println("Entrez la date de la première intervalle:");
			date1 = scanner.nextLine();
			System.out.println("Entrez la date de la seconde intervalle:");
			date2 = scanner.nextLine();
			procedureHistorique.setString(1, login);
			procedureHistorique.setString(2, date1);
			procedureHistorique.setString(3, date2);
			ResultSet res = procedureHistorique.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out.println("Date(s) invalide(s) ! !");
		}
	}

	private void supprimerAgent() {
		System.out.println("-	Entrez le login du nouvel agent:");
		String login = scanner.nextLine();
		PreparedStatement procedure = listeProcedures.get("supprimerAgent");
		try {
			procedure.setString(1, login);
			procedure.executeQuery();
			System.out
					.println("******************************************************");
			System.out
					.println("              Agent désormais inactif !               ");
			System.out
					.println("******************************************************");
		} catch (SQLException e) {
			System.out.println("Impossible de supprimer cet agent !");
		}
	}
	private void supprimerSh() {
		System.out.println("-	Entrez le nom de super-héros:");
		String nomSh = scanner.nextLine();
		PreparedStatement procedure = listeProcedures.get("supprimerSh");
		try {
			procedure.setString(1, nomSh);
		    procedure.executeQuery();
			System.out
					.println("******************************************************");
			System.out
					.println("Le Super-héros est à l'agonie... C'est fini pour lui !");
			System.out
					.println("******************************************************");
		} catch (SQLException e) {
			System.out.println("On l'a cherché partout dans notre SHYELD DATABASE mais on l'a jamais retrouvé mec ! C'est sûr, il doit avoir la cape d'invisibilité");
		}
	}

	private void voirShDisparus() {
		try {
			PreparedStatement selectShDisparus = listeSelects
					.get("superherosDisparus");
			ResultSet res = selectShDisparus.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("Il semblerait qu'il y ait eu une erreur avec les supers-héros !");
		}

	}
	
	private void classementSh() {
		try {
			PreparedStatement select = listeSelects
					.get("classementSh");
			ResultSet res = select.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("Il semblerait qu'il y ait eu une erreur avec les supers-héros !");
		}

	}
	
	private void classementShVictoires() {
		try {
			PreparedStatement select = listeSelects
					.get("classementShVictoires");
			ResultSet res = select.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("Il semblerait qu'il y ait eu une erreur avec les supers-héros !");
		}

	}
	
	private void classementShDefaites() {
		try {
			PreparedStatement select = listeSelects
					.get("classementShDefaites");
			ResultSet res = select.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("Il semblerait qu'il y ait eu une erreur avec les supers-héros !");
		}

	}
	
	private void classementAgents() {
		try {
			PreparedStatement select = listeSelects
					.get("classementAgents");
			ResultSet res = select.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("Il semblerait qu'il y ait eu une erreur avec les supers-héros !");
		}

	}
	private void avertissement() {
		PreparedStatement procedure = listeSelects.get("avertissement");
		try {
			ResultSet res = procedure.executeQuery();
			afficherRS(res);
		} catch (SQLException e) {
			System.out
					.println("La fonction d'avertissement semble capout :'( :'( :'( :'( :'( :'(  *Fin du monde*");
		}
	}

	private void afficherRS(ResultSet rs) throws SQLException {
		if (rs == null) {
			System.out.println("Pas de résultat");
			return;
		}
		ResultSetMetaData rsmd = rs.getMetaData();

		int nbColonnes = rsmd.getColumnCount();
		ArrayList<ArrayList<String>> resultat = new ArrayList<ArrayList<String>>();
		int tailleMaxColonne[] = new int[nbColonnes];
		ArrayList<String> ligne = new ArrayList<String>(nbColonnes);
		resultat.add(new ArrayList<String>(nbColonnes));
		for (int i = 1; i <= nbColonnes; i++) {
			resultat.get(0).add(rsmd.getColumnName(i));
			tailleMaxColonne[i - 1] = rsmd.getColumnName(i).length();
		}
		int rowCount = 1;
		while (rs.next()) {
			resultat.add(new ArrayList<String>());
			for (int i = 1; i <= nbColonnes; i++) {
				ligne = resultat.get(rowCount);
				ligne.add(rs.getString(i));
				if (ligne.get(i-1) != null && !ligne.get(i-1).isEmpty() && ligne.get(i - 1).length() > tailleMaxColonne[i - 1]) {
					tailleMaxColonne[i - 1] = ligne.get(i - 1).length();

				}
			}
			rowCount++;
		}
		if(rowCount == 1){
			System.out.println("Il n'y a aucune donnée !");
			return;
		}
		int nbLignes = resultat.size();
		String mot = "";
		int tailleMot = 0;
		int tailleMax = 0;
		String temp = "";
		for (int i = 0; i < nbLignes; i++) {
			ligne = resultat.get(i);
			for (int j = 0; j < nbColonnes; j++) {
				mot = ligne.get(j);
				temp += mot;
				if(mot != null && !mot.isEmpty()){
					tailleMot = mot.length();
				}
				else{
					tailleMot = 0;
				}
				tailleMax = tailleMaxColonne[j];
				for (int k = 0; k < tailleMax - tailleMot; k++) {
					temp += " ";
				}
				temp += " | ";
			}

			System.out.println(temp);
			temp = "";
		}
	}
	
	private byte lireByte(String message){
		byte entier;
		String line;
		do{
			System.out.println(message);
			try {
				line = scanner.nextLine();
				if(line.equals("") || line.isEmpty()){
					entier = 0;
					return entier;
				}
				entier = Byte.parseByte(line);
				return entier;
			} catch (NumberFormatException e) {
				System.out.println("Valeur incorrecte, réessayez !");
			}
		}while(true);
	}
	public static void main(String[] args) {
		Principale p = new Principale();
		System.out
				.println("******************************************************");
		System.out
				.println("                   WELCOME TO THE                     ");
		System.out
				.println("                       SHYELD                         ");
		System.out
				.println("******************************************************");
		byte menu;
		do {
			menu();
			menu = p.lireByte("Choix:");
			switch (menu) {
			case 0:
				p.deconnexion();
				break;
			case 1:
				p.enregistrerAgent();
				break;
			case 2:
				p.supprimerAgent();
				break;
			case 3:
				p.voirShDisparus();
				break;
			case 4:
				p.avertissement();
				break;
			case 5:
				p.voirHistoriqueCombatsAgent();
				break;
			case 6:
				p.voirHistoriqueReperagesAgent();
				break;
			case 7:
				p.classementSh();
				break;
			case 8:
				p.classementShVictoires();
				break;
			case 9:
				p.classementShDefaites();
				break;
			case 10:
				p.classementAgents();
				break;
			case 11: 
				p.supprimerSh();
				break;
			}
		} while (menu != 0);
	}

}
