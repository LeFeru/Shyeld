
DROP schema projet CASCADE ;
CREATE SCHEMA projet;
------------------------------------------------------------- CREATE TABLE ----------------------------------------------------------------------------------------------------------
CREATE TABLE projet.agents(
  id_agent SERIAL PRIMARY KEY CONSTRAINT Pk_Agent CHECK (id_agent > 0),
  nom VARCHAR(100) NOT NULL CONSTRAINT Nom_Agent CHECK (nom SIMILAR TO '[A-Za-z0-9]+[A-Za-z0-9 ]*'),
  prenom VARCHAR(100) NOT NULL CONSTRAINT Prenom_Agent CHECK (prenom SIMILAR TO '[A-Za-z0-9]+[A-Za-z0-9 ]*'),
  login VARCHAR(50) NOT NULL UNIQUE CONSTRAINT Login_Agent CHECK (login SIMILAR TO '[A-Za-z0-9]+[A-Za-z0-9_ .-]*'),
  mdp VARCHAR(10000000) NOT NULL CONSTRAINT Mdp_Agent CHECK (mdp <> ''),
  salt VARCHAR(10000000) NOT NULL CONSTRAINT Salt_Agent CHECK (salt <> ''),
  nb_reperages INTEGER NOT NULL DEFAULT 0 CONSTRAINT Nb_Reperages_Agent CHECK (nb_reperages > -1),
  inactif BOOLEAN DEFAULT FALSE NOT NULL
);
CREATE TABLE projet.super_heros (
  id_sh SERIAL PRIMARY KEY CONSTRAINT PK_Sh CHECK (id_sh > 0),
  nom_sh VARCHAR(50) NOT NULL CONSTRAINT NomSh_Sh CHECK (nom_sh SIMILAR TO '[A-Za-z0-9]+[A-Za-z0-9_ .-]*'),
  nom_civil VARCHAR(50) CONSTRAINT NomCivil_Sh CHECK (nom_civil SIMILAR TO '[A-Za-z0-9 ]*'),
  adresse_privee VARCHAR(100),-- CONSTRAINT AdressePrivee_Sh CHECK (adresse_privee <> ''),
  origine VARCHAR(50),-- CONSTRAINT Origine_Sh CHECK (origine <> ''),
  type_pouvoir VARCHAR(50),-- CONSTRAINT TypePouvoir_Sh CHECK (type_pouvoir <> ''),
  puissance_pouvoir INTEGER DEFAULT 1 CONSTRAINT PuissancePouvoir_Sh CHECK (puissance_pouvoir >= 0),
  faction VARCHAR(8) NOT NULL CONSTRAINT Faction_Sh CHECK (faction = 'M' OR faction = 'D'),
  derniere_x INTEGER DEFAULT 0 CONSTRAINT DerniereX_Sh CHECK (derniere_x >= 0 AND derniere_x <= 100),
  derniere_y INTEGER DEFAULT 0 CONSTRAINT DerniereY_Sh CHECK (derniere_y >= 0 AND derniere_y <= 100),
  derniere_date TIMESTAMP NOT NULL DEFAULT NOW() CONSTRAINT DerniereDate_Sh CHECK (derniere_date <= NOW()),
  mort BOOLEAN DEFAULT FALSE NOT NULL,
  nb_participations_g INTEGER NOT NULL DEFAULT 0 CONSTRAINT Nb_Participations_G_Sh CHECK (nb_participations_g > -1),
  nb_participations_p INTEGER NOT NULL DEFAULT 0 CONSTRAINT Nb_Participations_P_Sh CHECK (nb_participations_p > -1),
  nb_participations INTEGER NOT NULL DEFAULT 0 CONSTRAINT Nb_Participations_Sh CHECK (nb_participations > -1)
);
CREATE TABLE projet.reperages(
    id_reperage SERIAL PRIMARY KEY CONSTRAINT Pk_Reperage CHECK (id_reperage > 0),
    id_agent INTEGER REFERENCES projet.agents (id_agent) NOT NULL CONSTRAINT Fk_Agent_Reperage CHECK (id_agent > 0),
    id_sh INTEGER REFERENCES projet.super_heros(id_sh) NOT NULL CONSTRAINT Fk_Sh_Reperage CHECK (id_sh > 0),
    date_reperage TIMESTAMP NOT NULL DEFAULT NOW() CONSTRAINT DateReperage_Reperage CHECK (date_reperage <= NOW()),
    x INTEGER CONSTRAINT X_Reperage CHECK (x >= 0 AND x <= 100),
    y INTEGER CONSTRAINT Y_Reperage CHECK (y >= 0 AND y <= 100)
);
CREATE TABLE projet.combats(
  id_combat SERIAL  PRIMARY KEY CONSTRAINT Pk_Combat CHECK (id_combat > 0),
  id_agent INTEGER REFERENCES projet.agents(id_agent) NOT NULL CONSTRAINT Fk_Agent_Combat CHECK (id_agent > 0),
  x INTEGER CONSTRAINT X_Combat CHECK(x >= 0 AND x <=100),
  y INTEGER CONSTRAINT Y_Combat CHECK(y >= 0 AND y <=100),
  date_combat TIMESTAMP NOT NULL DEFAULT NOW() CONSTRAINT DateCombat_Combat CHECK (date_combat <= NOW())
);
CREATE TABLE projet.participations(
  id_combat INTEGER REFERENCES projet.combats(id_combat) NOT NULL CONSTRAINT Pk_Fk_Combat_Participation CHECK (id_combat > 0),
  id_sh INTEGER REFERENCES projet.super_heros(id_sh)  NOT NULL CONSTRAINT Pk_Fk_Sh_Participation CHECK (id_sh > 0),
  etat CHAR(1)  DEFAULT 'N' NOT NULL CONSTRAINT Etat_Participation CHECK (etat = 'G' OR etat = 'N' OR etat = 'P'),
  PRIMARY KEY (id_combat, id_sh)
);

------------------------------------------------------------- TRIGGER  ----------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION projet.majDernierReperage() RETURNS TRIGGER AS $$
DECLARE
BEGIN
  UPDATE projet.super_heros SET (derniere_date, derniere_x, derniere_y) = (NEW.date_reperage, NEW.x, NEW.y) WHERE projet.super_heros.id_sh = NEW.id_sh AND projet.super_heros.mort <> TRUE;
  UPDATE projet.agents SET (nb_reperages) = (nb_reperages+1) WHERE projet.agents.id_agent = NEW.id_agent;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER majDernierReperage AFTER INSERT ON projet.reperages FOR EACH ROW EXECUTE PROCEDURE projet.majDernierReperage();
CREATE OR REPLACE FUNCTION projet.majDernierReperageParticipant() RETURNS TRIGGER AS $$
DECLARE
  _combat RECORD;
BEGIN
  SELECT c.* FROM projet.combats c WHERE c.id_combat = NEW.id_combat INTO _combat;
  INSERT INTO projet.reperages VALUES (DEFAULT, _combat.id_agent, NEw.id_sh, DEFAULT, _combat.x, _combat.y);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER majDernierReperageParticipant AFTER INSERT ON projet.participations FOR EACH ROW EXECUTE PROCEDURE projet.majDernierReperageParticipant();
CREATE OR REPLACE FUNCTION projet.majNbParticipationsGNP() RETURNS TRIGGER AS $$
DECLARE
  toMaj RECORD;
BEGIN
  SELECT sh.nb_participations_g, sh.nb_participations_p, sh.nb_participations FROM projet.super_heros sh WHERE sh.id_sh = NEW.id_sh AND sh.mort <> TRUE INTO toMaj;
  CASE NEW.etat
    WHEN 'G' THEN
       UPDATE projet.super_heros SET (nb_participations_g, nb_participations) = (toMaj.nb_participations_g+1, toMaj.nb_participations+1) WHERE projet.super_heros.id_sh = NEW.id_sh AND projet.super_heros.mort <> TRUE;
    WHEN 'P' THEN
       UPDATE projet.super_heros SET (nb_participations_p, nb_participations) = (toMaj.nb_participations_p+1, toMaj.nb_participations+1) WHERE projet.super_heros.id_sh = NEW.id_sh AND projet.super_heros.mort <> TRUE;
    ELSE
       UPDATE projet.super_heros SET (nb_participations) = (toMaj.nb_participations+1) WHERE projet.super_heros.id_sh = NEW.id_sh AND projet.super_heros.mort <> TRUE;
    END CASE;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER majNbParticipationsGNP AFTER INSERT ON projet.participations FOR EACH ROW EXECUTE PROCEDURE projet.majNbParticipationsGNP();
CREATE OR REPLACE FUNCTION projet.verifierMort() RETURNS TRIGGER AS $$
DECLARE
BEGIN
  IF EXISTS(SELECT sh.nom_sh FROM projet.super_heros sh WHERE sh.nom_sh = NEW.nom_sh AND sh.mort = FALSE)THEN
    RAISE EXCEPTION 'Ce super hero existe deja  !';
  ELSE
     RETURN NEW;
  END IF;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER verifierMort BEFORE INSERT ON projet.super_heros FOR EACH ROW EXECUTE PROCEDURE projet.verifierMort();
CREATE OR REPLACE FUNCTION projet.ajouterAgent(CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING) RETURNS INTEGER AS $$
  DECLARE
    nom ALIAS FOR $1;
    prenom ALIAS FOR $2;
    _login ALIAS FOR $3;
    mdp ALIAS FOR $4;
    salt ALIAS FOR $5;
    id INTEGER;
  BEGIN
    INSERT INTO projet.agents VALUES (DEFAULT, nom, prenom, _login, mdp, salt, DEFAULT, DEFAULT)  RETURNING id_agent INTO id;
    RETURN id;
  END;
$$ LANGUAGE plpgsql;

------------------------------------------------------------- PROCEDURES ----------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION projet.ajouterSHComplet(CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING, INTEGER, CHARACTER) RETURNS INTEGER AS $$
  DECLARE
    _nom_sh ALIAS FOR $1;
    _nom_civil ALIAS FOR $2;
    _adresse_privee ALIAS FOR $3;
    _origine ALIAS FOR $4;
    _type_pouvoir ALIAS FOR $5;
    _puissance_pouvoir ALIAS FOR $6;
    _f ALIAS FOR $7;
    _faction CHARACTER;
    id INTEGER;
  BEGIN
   _faction := upper(_f);
   INSERT INTO projet.super_heros VALUES(DEFAULT,_nom_sh,_nom_civil,_adresse_privee,_origine,_type_pouvoir,_puissance_pouvoir,_faction,NULL,NULL,DEFAULT,DEFAULT,DEFAULT,DEFAULT,DEFAULT) RETURNING id_sh INTO id;
   RETURN id;
  END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.ajouterReperage(INTEGER, CHARACTER VARYING, INTEGER, INTEGER) RETURNS INTEGER AS $$
  DECLARE
    _id_agent ALIAS FOR $1;
    _nom_sh ALIAS FOR $2;
    _x ALIAS FOR $3;
    _y ALIAS FOR $4;
    _id_sh INTEGER;
    id INTEGER;
  BEGIN
    SELECT projet.getIdSh(_nom_sh) INTO _id_sh;
    PERFORM * FROM projet.agents a WHERE a.id_agent = _id_agent AND a.inactif <> TRUE;
    IF FOUND THEN
      INSERT INTO projet.reperages VALUES (DEFAULT, _id_agent, _id_sh, DEFAULT, _x, _y)  RETURNING id_reperage INTO id;
    ELSE
      RAISE EXCEPTION 'L''agent est inactif';
    END IF;
    RETURN id;
  END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.ajouterCombat(INTEGER, INTEGER, INTEGER) RETURNS INTEGER AS $$
DECLARE
  _id_agent ALIAS FOR $1;
  _x ALIAS FOR $2;
  _y ALIAS FOR $3;
  exist RECORD;
  id INTEGER;
BEGIN
  PERFORM * FROM projet.agents a WHERE a.id_agent = _id_agent AND a.inactif <> TRUE;
  IF FOUND THEN
    INSERT INTO projet.combats VALUES(DEFAULT, _id_agent, _x, _y, DEFAULT ) RETURNING projet.combats.id_combat INTO id;
  ELSE
    RAISE EXCEPTION 'L''agent est inactif';
  END IF;
  RETURN id;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.getIdSh(CHARACTER VARYING) RETURNS INTEGER AS $$
  DECLARE
    _nom_sh ALIAS FOR $1;
    id INTEGER;
  BEGIN
    SELECT sh.id_sh FROM projet.super_heros sh WHERE sh.nom_sh = _nom_sh AND sh.mort <> TRUE INTO id;
    RETURN id;
  END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.ajouterParticipation(INTEGER, INTEGER,CHARACTER VARYING, CHARACTER) RETURNS INTEGER AS $$
DECLARE
  _id_combat ALIAS FOR $1;
  _id_agent ALIAS FOR $2;
  _nom_sh ALIAS FOR $3;
  _etat ALIAS FOR $4;
  _id_sh INTEGER;
  id INTEGER;
  BEGIN
    SELECT projet.getIdSh(_nom_sh) INTO _id_sh;
    PERFORM * FROM projet.agents a WHERE a.id_agent = _id_agent AND a.inactif <> TRUE;
    IF FOUND THEN
      IF EXISTS(SELECT c.* FROM projet.combats c WHERE c.id_combat = _id_combat AND c.id_agent = _id_agent) THEN
          INSERT INTO projet.participations VALUES(_id_combat, _id_sh, _etat) RETURNING id_combat INTO id;
          RETURN id;
      END IF;
    ELSE
      RAISE EXCEPTION 'L''agent est mort';
    END IF;
    RAISE EXCEPTION 'Ce combat n''a pas été déclaré par cet agent ! %',id;
  END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.supprimerCombat(INTEGER) RETURNS VOID AS $$
DECLARE
  _id_combat ALIAS FOR $1;
  last_id_combat INTEGER;
BEGIN
  --last_id_combat := currval('projet.combats_id_combat_seq')-1:
  DELETE FROM projet.combats p WHERE p.id_combat = _id_combat;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.supprimerAgent(CHARACTER VARYING) RETURNS VOID AS $$
DECLARE
  _login ALIAS FOR $1;
BEGIN
  UPDATE projet.agents SET (inactif) = (TRUE) WHERE projet.agents.login = _login;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.supprimerSh(CHARACTER VARYING) RETURNS VOID AS $$
DECLARE
  _nom_sh ALIAS FOR $1;
  _id INTEGER;
BEGIN
  SELECT sh.id_sh FROM projet.super_heros sh WHERE sh.nom_sh = _nom_sh AND sh.mort = FALSE INTO _id;
  IF _id IS NOT NULL THEN
    UPDATE projet.super_heros SET(mort) = (TRUE) WHERE projet.super_heros.id_sh = _id;
    RETURN;
  END IF;
  RAISE EXCEPTION 'Le super-héros n''existe pas ou est déjà mort !';
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION projet.historiqueCombatsAgent(CHARACTER VARYING, CHARACTER VARYING) RETURNS TABLE(login VARCHAR, id_combat INTEGER, gagnants TEXT, nuls TEXT, perdants TEXT, x INTEGER, y INTEGER, date_combat TIMESTAMP) AS $$
DECLARE
  _date1 ALIAS FOR $1;
  _date2 ALIAS FOR $2;
BEGIN
  RETURN QUERY SELECT a.login, c.id_combat, hcg.participants, hcn.participants, hcp.participants, c.x, c.y, c.date_combat
FROM projet.agents a, projet.combats c
  LEFT OUTER JOIN projet.historiqueCombatsPerdants hcp ON hcp.id_combat = c.id_combat
  LEFT OUTER JOIN projet.historiqueCombatsNuls hcn ON hcn.id_combat = c.id_combat
  LEFT OUTER JOIN projet.historiqueCombatsGagnants hcg ON c.id_combat = hcg.id_combat
WHERE (a.id_agent = c.id_agent) AND c.date_combat >= to_timestamp('2010', 'YYYY-MM-DD hh24:mi:ss') AND c.date_combat <= to_timestamp('2020','YYYY-MM-DD hh24:mi:ss') ORDER BY c.date_combat;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE VIEW projet.historiqueReperages(login_agent, nom_sh, x, y, date_reperage) AS
  SELECT a.login, sh.nom_sh, r.x, r.y, r.date_reperage
  FROM projet.super_heros sh, projet.reperages r, projet.agents a
  WHERE sh.id_sh = r.id_sh
        AND r.id_agent = a.id_agent;
CREATE OR REPLACE FUNCTION projet.historiqueReperagesAgent(CHARACTER VARYING, CHARACTER VARYING, CHARACTER VARYING) RETURNS TABLE(nom_sh CHARACTER VARYING, x INTEGER, y INTEGER, date_reperage TIMESTAMP) AS $$
DECLARE
  _login_agent ALIAS FOR $1;
  _date1 ALIAS FOR $2;
  _date2 ALIAS FOR $3;
BEGIN
  RETURN QUERY SELECT hr.nom_sh , hr.x, hr.y, hr.date_reperage FROM projet.historiqueReperages hr WHERE _login_agent = hr.login_agent AND hr.date_reperage >= to_timestamp(_date1, 'YYYY-MM-DD hh24:mi:ss') AND hr.date_reperage <= to_timestamp(_date2,'YYYY-MM-DD hh24:mi:ss') ORDER BY hr.date_reperage;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------------------------------------- VUES ----------------------------------------------------------------------------------------------------------
CREATE OR REPLACE VIEW projet.superherosDisparus(numéro,nom,posX,posY)
AS SELECT sh.id_sh, sh.nom_sh,sh.derniere_x,sh.derniere_y
FROM projet.super_heros sh
WHERE sh.mort = FALSE AND sh.derniere_date <= TIMESTAMP 'NOW()' - interval'360 hours';
CREATE OR REPLACE VIEW projet.marvellesMoins10Jours(id_sh,nom_sh,derniere_x,derniere_y)
AS SELECT sh.id_sh, sh.nom_sh,sh.derniere_x,sh.derniere_y
FROM projet.super_heros sh
WHERE sh.mort = FALSE AND sh.faction = 'M' AND sh.derniere_date >= TIMESTAMP 'NOW()' - interval'240 hours';
CREATE OR REPLACE VIEW projet.deceMoins10Jours(id_sh,nom_sh,derniere_x,derniere_y)
AS SELECT sh.id_sh, sh.nom_sh,sh.derniere_x,sh.derniere_y
FROM projet.super_heros sh
WHERE sh.mort = FALSE AND sh.faction = 'D' AND sh.derniere_date >= TIMESTAMP 'NOW()' - interval'240 hours';
CREATE OR REPLACE VIEW projet.avertissement(derniere_x_marvelle,derniere_y_marvelle,derniere_x_dece,derniere_y_dece) AS
SELECT m.derniere_x, m.derniere_y, d.derniere_x, d.derniere_y
FROM projet.marvellesMoins10Jours m, projet.deceMoins10Jours d
WHERE (m.derniere_x = d.derniere_x OR m.derniere_x = d.derniere_x-1 OR m.derniere_x = d.derniere_x+1)
AND  (m.derniere_y = d.derniere_y OR m.derniere_y = d.derniere_y-1 OR m.derniere_y = d.derniere_y+1)
GROUP BY m.derniere_x, m.derniere_y, d.derniere_x, d.derniere_y;
CREATE OR REPLACE VIEW projet.historiqueCombatsGagnants(login_agent, id_combat, participants, x, y, date_combat) AS
  SELECT a.login, c.id_combat, string_agg(sh.nom_sh, ', '), c.x, c.y, c.date_combat
  FROM projet.super_heros sh, projet.combats c, projet.participations p, projet.agents a
  WHERE sh.id_sh = p.id_sh
        AND p.id_combat = c.id_combat
        AND c.id_agent = a.id_agent
        AND p.etat = 'G'
  GROUP BY a.login, c.id_combat, c.x, c.y, c.date_combat;
CREATE OR REPLACE VIEW projet.historiqueCombatsPerdants(login_agent, id_combat, participants, x, y, date_combat) AS
  SELECT a.login, c.id_combat, string_agg(sh.nom_sh, ', '), c.x, c.y, c.date_combat
  FROM projet.super_heros sh, projet.combats c, projet.participations p, projet.agents a
  WHERE sh.id_sh = p.id_sh
        AND p.id_combat = c.id_combat
        AND c.id_agent = a.id_agent
        AND p.etat = 'P'
  GROUP BY a.login, c.id_combat, c.x, c.y, c.date_combat;
CREATE OR REPLACE VIEW projet.historiqueCombatsNuls(login_agent, id_combat, participants, x, y, date_combat) AS
  SELECT a.login, c.id_combat, string_agg(sh.nom_sh, ', '), c.x, c.y, c.date_combat
  FROM projet.super_heros sh, projet.combats c, projet.participations p, projet.agents a
  WHERE sh.id_sh = p.id_sh
        AND p.id_combat = c.id_combat
        AND c.id_agent = a.id_agent
        AND p.etat = 'N'
  GROUP BY a.login, c.id_combat, c.x, c.y, c.date_combat;

CREATE OR REPLACE VIEW projet.classementSh(id_sh, nom_sh, nb_victoires, nb_defaites, nb_participations) AS
  SELECT sh.id_sh, sh.nom_sh, sh.nb_participations_g, sh.nb_participations_p, sh.nb_participations
  FROM projet.super_heros sh
  ORDER BY sh.nb_participations_g DESC, sh.nb_participations_p ASC, sh.nb_participations ASC;
CREATE OR REPLACE VIEW projet.classementShVictoires(id_sh, nom_sh, nb_victoires) AS
  SELECT sh.id_sh, sh.nom_sh, sh.nb_participations_g
  FROM projet.super_heros sh
  ORDER BY sh.nb_participations_g DESC, sh.nb_participations ASC;
CREATE OR REPLACE VIEW projet.classementShDefaites(id_sh, nom_sh, nb_defaites) AS
  SELECT sh.id_sh, sh.nom_sh, sh.nb_participations_p
  FROM projet.super_heros sh
  ORDER BY sh.nb_participations_p DESC, sh.nb_participations ASC;
CREATE OR REPLACE VIEW projet.classementAgents(id, login, nb_reperages) AS
  SELECT a.id_agent, a.login, a.nb_reperages
  FROM projet.agents a
  ORDER BY a.nb_reperages DESC;

ALTER SEQUENCE projet.agents_id_agent_seq RESTART WITH 1;
ALTER SEQUENCE projet.super_heros_id_sh_seq RESTART WITH 1;
ALTER SEQUENCE projet.reperages_id_reperage_seq RESTART WITH 1;
ALTER SEQUENCE projet.combats_id_combat_seq RESTART WITH 1;


SELECT * FROM projet.historiqueCombatsAgent('2010', '2020');
--GRANT CONNECT ON DATABASE dbrasli15 TO mmzough15;
--GRANT Usage ON SCHEMA projet TO mmzough15;
--GRANT SELECT ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT INSERT ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT UPDATE ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT SELECT ON  projet.superherosDisparus, projet.marvellesMoins10Jours, projet.deceMoins10Jours, projet.avertissement, projet.historiqueCombatsGagnants, projet.historiqueCombatsPerdants, projet.historiqueReperages, projet.classementSh, projet.classementShVictoires, projet.classementShDefaites, projet.classementAgents TO mmzough15;
--GRANT SELECT, UPDATE ON SEQUENCE projet.agents_id_agent_seq, projet.combats_id_combat_seq, projet.reperages_id_reperage_seq, projet.super_heros_id_sh_seq TO mmzough15;
--projet.ajouterAgent, projet.ajouterSH, projet.SHComplet, projet.ajouterReperage, projet.ajouterCombat, projet.getIdSh, projet.ajouterParticipation, projet.supprimerCombat, projet.supprimerAgent, projet.supprimerShDisparu, projet.supprimerSh, projet.superherosDisparus, projet.supprimerSh, projet.historiqueCombatsAgent, projet.historiqueReperagesAgent,

--------------------------------------------------- AJOUT DES SUPER HERO POUR LE SCENARIO --------------------------------------------------------------------------------------------------------------
INSERT INTO projet.super_heros(nom_sh,nom_civil,adresse_privee,origine,type_pouvoir, puissance_pouvoir,faction,derniere_x,derniere_y)VALUES('Dramas','Docteur','Rue hippocrate','piqure','guerison',2,'D',NULL,NULL);
INSERT INTO projet.super_heros(nom_sh,nom_civil,adresse_privee,origine,type_pouvoir, puissance_pouvoir,faction,derniere_x,derniere_y)VALUES('Faire Mieux','Madame','Rue Du meilleur','Tombée sur la tete','Math',56,'D',NULL,NULL);
INSERT INTO projet.super_heros(nom_sh,nom_civil,adresse_privee,origine,type_pouvoir, puissance_pouvoir,faction,derniere_x,derniere_y)VALUES('Gloriaux','Donnateur','Rue de la gloire & boté','Injection','SQL',85,'M',NULL,NULL);
INSERT INTO projet.super_heros(nom_sh,nom_civil,adresse_privee,origine,type_pouvoir, puissance_pouvoir,faction,derniere_x,derniere_y)VALUES('Hulkriet','Henpleuriet','Rue de linux','Morsure de pinguin','C',90,'M',NULL,NULL);

--GRANT CONNECT ON DATABASE dbrasli15 TO mmzough15;
--GRANT Usage ON SCHEMA projet TO mmzough15;
--GRANT SELECT ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT INSERT ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT UPDATE ON projet.agents,projet.combats,projet.participations,projet.reperages,projet.super_heros TO mmzough15;
--GRANT SELECT ON  projet.superherosDisparus, projet.marvellesMoins10Jours, projet.deceMoins10Jours, projet.avertissement, projet.historiqueCombatsGagnants, projet.historiqueCombatsPerdants, projet.historiqueReperages, projet.classementSh, projet.classementShVictoires, projet.classementShDefaites, projet.classementAgents TO mmzough15;
--GRANT SELECT, UPDATE ON SEQUENCE projet.agents_id_agent_seq, projet.combats_id_combat_seq, projet.reperages_id_reperage_seq, projet.super_heros_id_sh_seq TO mmzough15;
--projet.ajouterAgent, projet.ajouterSH, projet.SHComplet, projet.ajouterReperage, projet.ajouterCombat, projet.getIdSh, projet.ajouterParticipation, projet.supprimerCombat, projet.supprimerAgent, projet.supprimerShDisparu, projet.supprimerSh, projet.superherosDisparus, projet.supprimerSh, projet.historiqueCombatsAgent, projet.historiqueReperagesAgent,
