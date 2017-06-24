import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

public class Agent {
	public static java.util.Scanner scanner = new java.util.Scanner(System.in);
	private Connection conn = null;
	private String user = "mmzough15"; // Seulement pour les tests
	private String password = "B6AFuY6"; // Seulement pour les tests
	private HashMap<String, PreparedStatement> listeSelects;
	private HashMap<String, PreparedStatement> listeProcedures;
	private HashMap<String, String> listeErreurs;
	private int id;

	public Agent() {
		connexion();
		setSelects();
		setProcedures();
		setErreurs();
		id = -1;
	}

	private void connexion() {
		System.out.println("Initialisation de la connexion:");
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Driver PostgreSQL OK !");
			String url = "jdbc:postgresql://172.24.2.6:5432/dbrasli15?user=" + user + "&password=" + password;
			// String url = "jdbc:postgresql://localhost:5434/projet?user=" +
			// user + "&password=" + password;
			try {
				conn = DriverManager.getConnection(url);
				System.out.println("Connecté au serveur ! Avec " + conn.getMetaData().getDriverName() + " "
						+ conn.getMetaData().getDriverVersion() + "{ " + conn.getMetaData().getDriverMajorVersion()
						+ "," + conn.getMetaData().getDriverMinorVersion() + " }" + " to "
						+ conn.getMetaData().getDatabaseProductName() + " "
						+ conn.getMetaData().getDatabaseProductVersion() + "\n");
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

	private void deconnexion() {
		try {
			conn.close();
			System.out.println("******************************************************");
			System.out.println("               Déconnecté du serveur                  ");
			System.out.println("******************************************************");
			System.exit(0);
		} catch (SQLException e) {
			System.out.println("Echec lors de la déconnexion");
		}
	}

	private void setSelects() {
		try {
			listeSelects = new HashMap<String, PreparedStatement>();
			listeSelects.put("agents", conn.prepareStatement("SELECT a.* FROM projet.agents a"));
			listeSelects.put("combats", conn.prepareStatement("SELECT c.* FROM projet.combats c"));
			listeSelects.put("participations", conn.prepareStatement("SELECT p.* FROM projet.participations p"));
			listeSelects.put("reperages", conn.prepareStatement("SELECT r.* FROM projet.reperages r"));
			listeSelects.put("super_heros", conn.prepareStatement("SELECT sh.* FROM projet.super_heros sh"));
			listeSelects.put("agent", conn.prepareStatement("SELECT a.* FROM projet.agents a WHERE a.id_agent = ?"));
			listeSelects.put("combat", conn.prepareStatement("SELECT c.* FROM projet.combats c WHERE c.id_combat = ?"));
			listeSelects.put("reperage",
					conn.prepareStatement("SELECT r.* FROM projet.reperages r WHERE r.id_reperage = ?"));
			listeSelects.put("superheros",
					conn.prepareStatement("SELECT sh.* FROM projet.super_heros sh WHERE sh.id_sh = ?"));
			listeSelects.put("combats/agent",
					conn.prepareStatement("SELECT c.* FROM projet.combats c WHERE c.id_agent = ?"));
			listeSelects.put("getIdAgent",
					conn.prepareStatement("SELECT a.id_agent FROM projet.agents a WHERE a.login = ?"));
			listeSelects.put("superherosDisparus", conn.prepareStatement("SELECT * FROM projet.superherosDisparus;"));
			listeSelects.put("getIdShTous",
					conn.prepareStatement("SELECT sh.id_sh FROM projet.super_heros sh WHERE sh.nom_sh = ?"));
		} catch (SQLException e) {
			System.out.println("Un ou plusieurs prepareStatement a renvoyé une SQLException");
			System.exit(1);
		}
	}

	private void setProcedures() {
		listeProcedures = new HashMap<String, PreparedStatement>();
		try {
			listeProcedures.put("ajouterAgent", conn.prepareStatement("SELECT * FROM projet.ajouterAgent(?,?,?,?,?);"));
			listeProcedures.put("getIdAgent", conn.prepareStatement("SELECT * FROM projet.getIdAgent(?);"));
			listeProcedures.put("ajouterSHComplet",
					conn.prepareStatement("SELECT * FROM projet.ajouterSHComplet(?, ?, ?, ?, ?, ?, ?);"));
			listeProcedures.put("ajouterReperage",
					conn.prepareStatement("SELECT * FROM projet.ajouterReperage(?, ?, ?, ?);"));
			listeProcedures.put("ajouterCombat", conn.prepareStatement("SELECT * FROM projet.ajouterCombat(?, ?, ?);"));
			listeProcedures.put("ajouterParticipation",
					conn.prepareStatement("SELECT * FROM projet.ajouterParticipation(?, ?, ?, ?);"));
			listeProcedures.put("getIdSh", conn.prepareStatement("SELECT * FROM projet.getIdSh(?);"));
			listeProcedures.put("supprimerSh", conn.prepareStatement("SELECT * FROM projet.supprimerSh(?);"));
		} catch (SQLException e) {
			System.out.println("Un ou plusieurs prepareStatement a renvoyé une SQLException");
			System.exit(1);
		}
	}

	private void setErreurs() {
		listeErreurs = new HashMap<String, String>();
		listeErreurs.put("null value in column \"id_sh\"", "Le super-héros n'existe pas");
		listeErreurs.put("Pk_Agent", "L'id de l'agent doit être un entier strictement positif et unique");
		listeErreurs.put("Nom_Agent",
				"Le nom de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs.put("Prenom_Agent",
				"Le prenom de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs.put("Login_Agent",
				"Le login de l'agent doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s), . , - , _");
		listeErreurs.put("Mdp_Agent", "Le mot de passe de l'agent ne peut être vide ou ne contenir que des espaces");
		listeErreurs.put("Salt_Agent", "Le Salt (key) de l'agent ne peut être vide ou ne contenir que des espaces");
		listeErreurs.put("Pk_Sh", "L'id du super-héros doit être un entier strictement positif et unique");
		listeErreurs.put("NomSh_Sh",
				"Le nom du super-héros doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s), . , - , _");
		listeErreurs.put("NomCivil_Sh",
				"Le nom civil du super-héros doit commencer soit par un caractère de l'alphabet (latin) soit par un entier entre 0 et 9 (inclus) et peut être suivit de zéro, un ou plusieurs: caractère(s) de ce même alphabet et/ou entier(s) entre 0 et 9 et/ou espace(s)");
		listeErreurs.put("AdressePrivee_Sh",
				"L'adresse privée du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs.put("Origine_Sh", "L'origine du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs.put("TypePouvoir_Sh",
				"Le type de pouvoir du super-héros ne peut être vide ou ne contenir que des espaces");
		listeErreurs.put("PuissancePouvoir_Sh",
				"La puissance du pouvoir du super-héros doit être un entier strictement positif");
		listeErreurs.put("Faction_Sh",
				"La faction du super-héros doit être 'M' (pour Marvelle) ou 'D' (pour Décé Comics)");
		listeErreurs.put("DerniereX_Sh",
				"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("DerniereY_Sh",
				"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("DerniereDate_Sh",
				"La date du dernier repérage ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs.put("Pk_Reperage", "L'id du repérage doit être un entier strictement positif et unique");
		listeErreurs.put("Fk_Agent_Reperage",
				"La clé étrangère de l'agent doit faire référence à une clé primaire existante dans la table agent");
		listeErreurs.put("Fk_Sh_Reperage ",
				"La clé étrangère du super-héros doit faire référence à une clé primaire existante dans la table super_heros");
		listeErreurs.put("DateReperage_Reperage",
				"La date du repérage ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs.put("X_Reperage",
				"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("Y_Reperage",
				"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("Pk_Combat", "L'id du combat doit être un entier strictement positif et unique");
		listeErreurs.put("Fk_Agent_Combat",
				"La clé étrangère de l'agent doit faire référence à une clé primaire existante dans la table agent");
		listeErreurs.put("X_Combat",
				"L'abscisse (x) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("Y_Combat",
				"La coordonnée (y) d'une position doit être un entier non-négatif et inférieur ou égal à 100");
		listeErreurs.put("DateCombat_Combat",
				"La date ne peut être dans le futur... A moins que vous ne puissiez voyager à travers le temps ? :o *Bave* #BTTF PS: T'aurais pas les questions d'examens au passage ?");
		listeErreurs.put("Pk_Fk_Combat_Participation",
				"La clé étrangère du combat doit faire référence à une clé primaire existante dans la table combat");
		listeErreurs.put("Pk_Fk_Sh_Participation",
				"La clé étrangère du super-héros doit faire référence à une clé primaire existante dans la table super_heros");
		listeErreurs.put("Etat_Participation",
				"L'état d'une participation à un combat est soit: G pour gagner, P pour perdu et N pour nul");
	}

	private static void menu() {
		System.out.println("******************************************************");
		System.out.println("-	Quitter la SHYELD APP (0)");
		System.out.println("-	Enregistrer la découverte d'un nouveau super-héros (1)");
		System.out.println("-	Enregistrer la position d'un super-héros (2)");
		System.out.println("-	Enregistrer un nouveau combat (3)");
		System.out.println("-	Voir info d'un super-héros(4)");
		System.out.println("-   Supprimer un super-héros(5)");

		System.out.println("******************************************************");
	}

	private void connecterAgent() {
		System.out.println("-	Entrez votre login d'agent:");
		String login = scanner.nextLine();
		System.out.println("-	Entrez votre mot de passe d'agent:");
		String mdp = scanner.nextLine();
		int id = getIdAgent(login);
		if (id != -1 && checkMdpAgent(id, mdp)) {
			boolean inactif;
			try {
				PreparedStatement procedureSelectAgent = listeSelects.get("agent");
				procedureSelectAgent.setInt(1, id);
				ResultSet res = procedureSelectAgent.executeQuery();
				res.next();
				inactif = res.getBoolean("inactif");
			} catch (SQLException e) {
				System.out.println("Il semblerait que cet agent soit très discret !");
				return;
			}
			if (!inactif) {
				this.id = id;
				System.out.println("******************************************************");
				System.out.println("                      Connecté                        ");
				System.out.println("******************************************************");
				return;
			}
			System.out.println("Par les lois de la nature, cet agent n'est plus parmi nous. :'( :'( :'(");
			return;
		}
		System.out.println("login et/ou mot de passe incorrects !");

	}

	private boolean estConnecte() {
		return id != -1;
	}

	private void deconnecterAgent() {
		id = -1;
		System.out.println("******************************************************");
		System.out.println("                     Déconnecté                       ");
		System.out.println("******************************************************");
	}

	private boolean checkMdpAgent(int idAgent, String mdp) {
		try {
			PreparedStatement procedure = listeSelects.get("agent");
			procedure.setInt(1, idAgent);
			ResultSet res = procedure.executeQuery();
			String mdpHashed;
			String salt;
			if (res.next()) {
				mdpHashed = res.getString("mdp");
				salt = res.getString("salt");
				if (mdpHashed.equals(BCrypt.hashpw(mdp, salt))) {
					return true;
				}
				return false;
			}
			return false;
		} catch (SQLException e) {
			System.out.println("Echec lors de la vérification !");
			return false;
		}
	}

	private int getIdAgent(String login) {
		PreparedStatement procedure = listeSelects.get("getIdAgent");
		try {
			procedure.setString(1, login);
			ResultSet res = procedure.executeQuery();
			if (res.next()) {
				return Integer.parseInt(res.getString("id_agent"));
			}
			return -1;
		} catch (SQLException e) {
			System.out.println("Cet agent n'existe pas !");
			return -1;
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
				if (ligne.get(i - 1) != null && !ligne.get(i - 1).isEmpty()
						&& ligne.get(i - 1).length() > tailleMaxColonne[i - 1]) {
					tailleMaxColonne[i - 1] = ligne.get(i - 1).length();

				}
			}
			rowCount++;
		}
		if (rowCount == 1) {
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
				if (mot != null && !mot.isEmpty()) {
					tailleMot = mot.length();
				} else {
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

	private void afficherException(String message) {
		if (message == null || message.isEmpty()) {
			System.out.println("Message vide");
		}
		for (String key : listeErreurs.keySet()) {
			if (message.toLowerCase().contains(key.toLowerCase())) {
				System.out.println("Violation de la contrainte; " + listeErreurs.get(key));
				return;
			}
		}
		System.out.println("Une erreur inopinée s'est produite :'(. Nos meilleurs techniciens sont sur le coup !! :D ");
	}

	private void ajouterSH() {
		if (id == -1) {
			System.out.println("Vous devez être connecté pour effectuer cet action");
			return;
		}
		System.out.println("Entrez le nom de super-héros :");
		String nom_sh = scanner.nextLine();
		System.out.println(
				"Entrez le nom civil de " + nom_sh + " (facultatif, taper sur la touche Entrée pour continuer) :");
		String nom_civil = scanner.nextLine();
		System.out.println("Entrez son adresse privée (facultatif, taper sur la touche Entrée pour continuer) :");
		String adresse_privee = scanner.nextLine();
		System.out.println(
				"Entrez l'origine de son super-pouvoir (facultatif, taper sur la touche Entrée pour continuer) :");
		String origine = scanner.nextLine();
		System.out.println("Entrez le type de super-pouvoir (facultatif, taper sur la touche Entrée pour continuer) :");
		String type_pouvoir = scanner.nextLine();
		int puissance = lireInt(
				"Entrez la puissance de son super-pouvoir (ne peut être négatif) (facultatif, taper sur la touche Entrée pour continuer) :");
		System.out.println("Entrez sa faction :");
		String faction = scanner.nextLine();
		System.out.println("As tu enregistré sa position ? (Entrez Oui ou Non)");
		String rep = scanner.nextLine();
		PreparedStatement procedure = listeProcedures.get("ajouterSHComplet");
		try {
			procedure.setString(1, nom_sh);
			procedure.setString(2, nom_civil);
			procedure.setString(3, adresse_privee);
			procedure.setString(4, origine);
			procedure.setString(5, type_pouvoir);
			procedure.setInt(6, puissance);
			procedure.setString(7, faction);
			ResultSet res = procedure.executeQuery();
			if (res.next()) {
				System.out.println("Le super-héros a été ajouté");
				if (rep.toUpperCase().equals("OUI")) {
					ajouterReperage(nom_sh);
				}
				return;
			}
			System.out.println("Ce super-héros existe déjà");
		} catch (SQLException e) {
			afficherException(e.getMessage());
		}
	}

	private void ajouterReperage() {
		if (id == -1) {
			System.out.println("Vous devez être connecté pour effectuer cet action");
			return;
		}
		System.out.println("Entrez le nom du super-héros repéré:");
		String nom_sh = scanner.nextLine();
		ajouterReperage(nom_sh);
	}

	private void ajouterReperage(String nom_sh) {
		if (id == -1) {
			System.out.println("Vous devez être connecté pour effectuer cet action");
			return;
		}
		byte x = lireByte("Entrez l'abscisse de sa position (x) :");
		byte y = lireByte("Entrez l'ordonnée de sa position (y) : ");
		PreparedStatement procedure = listeProcedures.get("ajouterReperage");
		try {
			procedure.setInt(1, id);
			procedure.setString(2, nom_sh);
			procedure.setByte(3, x);
			procedure.setByte(4, y);
			ResultSet res = procedure.executeQuery();
			if (res.next()) {
				System.out.println("La nouvelle position a bien été enregistré");
				return;
			}
			System.out.println(
					"Ce super-héros a déjà été reperé par vos soins à ce moment-là et cette position précisement.");
		} catch (SQLException e) {
			afficherException(e.getMessage());
		}
	}

	private void ajouterCombat() {
		if (id == -1) {
			System.out.println("Vous devez être connecté pour effectuer cet action");
			return;
		}
		byte x = lireByte("Entrez l'abscisse de sa position (x) :");
		byte y = lireByte("Entrez l'ordonnée de sa position (y) : ");
		PreparedStatement procedureAjouterCombat = listeProcedures.get("ajouterCombat");
		PreparedStatement procedureAjouterParticipant = listeProcedures.get("ajouterParticipation");
		PreparedStatement procedureSelectSh = listeSelects.get("superheros");
		PreparedStatement procedureGetIdSh = listeProcedures.get("getIdSh");
		PreparedStatement procedureReperage = listeProcedures.get("ajouterReperage");
		ArrayList<String> reperages = new ArrayList<String>();
		try {
			conn.setAutoCommit(false);
			procedureAjouterCombat.setInt(1, id);
			procedureAjouterCombat.setByte(2, x);
			procedureAjouterCombat.setByte(3, y);
			ResultSet res = procedureAjouterCombat.executeQuery();
			res.next();
			int idCombat = Integer.parseInt(res.getString("ajouterCombat"));
			boolean m = false;
			boolean d = false;
			char GNP;
			char faction = '\0';
			String nomSh;
			do {
				System.out.println(
						"Introduisez le nom de super-héros participant à ce combat (ou tapez STOP pour terminer):");
				nomSh = scanner.nextLine();
				if (!nomSh.equals("STOP")) {
					System.out.println("A-t-il gagné (G), perdu (P) ou aucun des deux (N) ?");
					GNP = scanner.nextLine().charAt(0);
					reperages.add(nomSh);
					try {
						procedureGetIdSh.setString(1, nomSh);
						res = procedureGetIdSh.executeQuery();
						res.next();
						procedureSelectSh.setInt(1, Integer.parseInt(res.getString(1)));
						res = procedureSelectSh.executeQuery();
						res.next();
						faction = res.getString("faction").charAt(0);
					} catch (Exception e) {
						System.out.println("Ce super-héros n'existe pas");
						continue;
					}
					if (faction == 'M') {
						m = true;
					} else {
						d = true;
					}
					procedureAjouterParticipant.setInt(1, idCombat);
					procedureAjouterParticipant.setInt(2, id);
					procedureAjouterParticipant.setString(3, nomSh);
					procedureAjouterParticipant.setString(4, "" + GNP);
					procedureAjouterParticipant.executeQuery();
				}
			} while (!nomSh.equals("STOP"));
			if (m & d) {
				conn.commit();
				conn.setAutoCommit(true);
				System.out.println("Combat ajouté avec succès");
				return;
			} else if (m || d) {
				conn.rollback();
				System.out.println(
						"Pour un combat, 2 factions opposées doivent s'affronter ! Néanmoins, le gouvernement vous remercie pour les nouvelles positions :)");
				for (String sh : reperages) {
					procedureReperage.setInt(1, id);
					procedureReperage.setString(2, sh);
					procedureReperage.setByte(3, x);
					procedureReperage.setByte(4, y);
					procedureReperage.executeQuery();
				}
				conn.commit();
				conn.setAutoCommit(true);
				return;
			} else {
				System.out.println("Le combat n'a pu être ajouté ");
				conn.rollback();
				conn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			System.out.println("Le combat n'a pu être ajouté ");
			return;
		}
	}

	private void voirInfosSh() {
		PreparedStatement getIdSh = listeProcedures.get("getIdSh");
		PreparedStatement listeinfo = listeSelects.get("superheros");
		int idSh;
		ResultSet res;
		String nomSh;
		try {
			System.out.println("Entrez le nom du super-héros dont vous désirez les informations");
			nomSh = scanner.nextLine();
			getIdSh.setString(1, nomSh);
			res = getIdSh.executeQuery();
			if (res.next()) {
				idSh = res.getInt(1);
				listeinfo.setInt(1, idSh);
				res = listeinfo.executeQuery();
				afficherRS(res);
			} else {
				System.out
						.println("Oh mais ce super hero n'existe pas !! Voudrais tu entrer ses infos s'il te plait :D");
				ajouterSH();
			}
		} catch (SQLException e) {
			afficherException(e.getMessage());
		}
	}

	private void supprimerSh() {
		System.out.println("-	Entrez le nom de super-héros:");
		String nomSh = scanner.nextLine();
		PreparedStatement procedure = listeProcedures.get("supprimerSh");
		try {
			procedure.setString(1, nomSh);
			procedure.executeQuery();
			System.out.println("******************************************************");
			System.out.println("Le Super-héros est à l'agonie... C'est fini pour lui !");
			System.out.println("******************************************************");
		} catch (SQLException e) {
			System.out.println(
					"On l'a cherché partout dans notre SHYELD DATABASE mais on l'a jamais retrouvé mec ! C'est sûr, il doit avoir la cape d'invisibilité");
		}
	}

	private int lireInt(String message) {
		int entier;
		String line;
		do {
			System.out.println(message);
			try {
				line = scanner.nextLine();
				if (line.equals("") || line.isEmpty()) {
					entier = 0;
					return entier;
				}
				entier = Integer.parseInt(line);
				return entier;
			} catch (NumberFormatException e) {
				System.out.println("Valeur incorrecte, réessayez !");
			}
		} while (true);
	}

	private byte lireByte(String message) {
		byte entier;
		String line;
		do {
			System.out.println(message);
			try {
				line = scanner.nextLine();
				if (line.equals("") || line.isEmpty()) {
					entier = 0;
					return entier;
				}
				entier = Byte.parseByte(line);
				return entier;
			} catch (NumberFormatException e) {
				System.out.println("Valeur incorrecte, réessayez !");
			}
		} while (true);
	}

	public static void main(String[] args) {
		Agent a = new Agent();
		System.out.println("******************************************************");
		System.out.println("                   WELCOME TO THE                     ");
		System.out.println("                  AGENT SHYELD APP                    ");
		System.out.println("******************************************************");
		a.connecterAgent();
		while (!a.estConnecte()) {
			a.connecterAgent();
		}
		byte menu;
		do {
			menu();
			menu = a.lireByte("Choix:");
			switch (menu) {
			case 0:
				a.deconnecterAgent();
				break;
			case 1:
				a.ajouterSH();
				break;
			case 2:
				a.ajouterReperage();
				break;
			case 3:
				a.ajouterCombat();
				break;
			case 4:
				a.voirInfosSh();
				break;
			case 5:
				a.supprimerSh();
				break;
			}
		} while (menu != 0);
		a.deconnexion();
	}

}
