#!/bin/bash

# Description:
#	Installation de Nextcloud 17.0.1 sur Debian Buster (10)
#----------------------------------------------------------------#
# Usage: ./install_nextcloud.sh
#	Exécuter le script en root!
#
# Auteurs:
# Aurélie Pouteau
# Jovial Ndongui
# Daniel Dos Santos
# Killian Boulard
#----------------------------------------------------------------#

apt update && apt full-upgrade -y

# Variables
vernext="17.0.1"
verphp="7.3"

# identiants mariadb pour nextcloud
mariadatabase="nextcloud"
mariauser="daniel"
mariapasswd="efficom"

ipnet=$(hostname -I | awk '{print $1}')
usertos=$(w | awk '{print $1}' | awk 'NR==3')

# Fonctions

#installation des dépendances
apt install apache2  -y
apt install mariadb-server  -y
apt install php"$verphp" -y
apt install php-xml  -y
apt install php-cli  -y
apt install php-cgi  -y
apt install php-mysql  -y
apt install php-mbstring  -y
apt install php-gd  -y
apt install php-curl  -y
apt install php-zip  -y

systemctl restart apache2

# Configuration de PHP

cp /etc/php/$verphp/apache2/php.ini /etc/php/$verphp/apache2/php.ini.save

echo "" > /etc/php/$verphp/apache2/php.ini

echo "
date.timezone = Europe/France
memory_limit = 512M
upload_max_filesize = 10240M
post_max_size = 500M
max_execution_time = 300
max_input_time = 600
" > /etc/php/$verphp/apache2/php.ini

systemctl restart apache2

# Configuration de MariaDB

echo "
Suivre les instructions:
1 Définir un nouveau mdp ou garder le mot de passe du système. 
2 Effacer l’utilisateur anonymous pour des raisons de sécurité.
3 Désactiver la connexion sur la base de donnée avec le compte root à distance.
4 Effacer les bases de données de test.
5 Redémarrer les tables de droits.
"
sleep 5

mysql_secure_installation

mysql -u root -p << EOT      
CREATE DATABASE "$mariadatabase";
CREATE USER "$mariauser"@'localhost' IDENTIFIED BY "$mariapasswd";
GRANT ALL ON "$mariadatabase".* TO "$mariauser"@'localhost';
FLUSH PRIVILEGES;
> EOT     

# Installation de Nextcloud
wget https://download.nextcloud.com/server/releases/nextcloud-"$vernext".zip

# Vérification de l’intégrité du document téléchargé
wget https://download.nextcloud.com/server/releases/nextcloud-"$vernext".zip.sha256
sha256sum  -c nextcloud-"$vernext".zip.sha256 < nextcloud-"$vernext".zip

# Extraction
apt install unzip
unzip nextcloud-"$vernext".zip

cp -R nextcloud /var/www/html/

# Configuration du serveur web
touch /etc/apache2/sites-available/default-ssl.conf
###
"###
####
####
####
systemctl reload apache2

a2enmod rewrite
systemctl reload apache2

a2enmod headers
service apache2 restart

a2enmod ssl
service apache2 restart

a2ensite default-ssl
service apache2 reload

chown -R www-data:www-data /var/www/html/nextcloud/

sudo -u "$usertos" firefox http://"$ipnet"/nextcloud
