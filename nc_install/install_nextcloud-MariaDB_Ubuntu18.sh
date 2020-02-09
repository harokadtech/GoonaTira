#!/bin/bash

#Install Nextcloud 17 sur ubuntu 18.04
#https://www.c-rieger.de/nextcloud-installation-guide-ubuntu-18-04/
#Conteneur Proxmox: Ubuntu-18.04-standard_18.04.1-1_amd64.tar.gz

apt update && apt full-upgrade -y

# Restart services during package upgrades without asking? Yes

# dependances
apt install curl -y
apt install gnupg2 -y
apt install git -y
apt install apt-transport-https -y
apt install tree -y
apt install locate -y
apt install software-properties-common -y
apt install screen -y
apt install htop -y
apt install zip -y
apt install ffmpeg -y
apt install ghostscript -y
apt install libfile-fcntllock-perl -y

# install Apache2
apt install apache2 -y

# install PHP
add-apt-repository ppa:ondrej/php -y
apt update -y
apt install php7.3 -y
apt install php7.3-fpm -y
apt install php7.3-gd -y
apt install php7.3-mysql -y
apt install php7.3-curl -y
apt install php7.3-xml -y
apt install php7.3-zip -y
apt install php7.3-intl -y
apt install php7.3-mbstring -y
apt install php7.3-bz2 -y
apt install php7.3-ldap -y
apt install php-apcu -y
apt install imagemagick -y
apt install php-imagick -y
apt install php-smbclient -y

# Configuration de PHP

timedatectl set-timezone Europe/Paris

date

cp /etc/php/$verphp/apache2/php.ini /etc/php/$verphp/apache2/php.ini.save


sed -i 's/memory_limit = 128M/memory_limit = 512M/' /etc/php/7.3/apache2/php.ini
sed -i 's/upload_max_filesize = 2M/upload_max_filesize = 1024M/' /etc/php/7.3/apache2/php.ini
sed -i 's/post_max_size = 8M/post_max_size = 1024M/' /etc/php/7.3/apache2/php.ini
sed -i 's/max_execution_time = 30/max_execution_time = 300/' /etc/php/7.3/apache2/php.ini
sed -i 's/max_input_time = 60/max_input_time = 600/' /etc/php/7.3/apache2/php.ini
sed -i 's/memory_limit = 128M/memory_limit = 512M/' /etc/php/7.3/apache2/php.ini


systemctl restart apache2

# install MariaDB
apt install mariadb-server -y

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

mysql -u root -pdigital << EOT      
CREATE DATABASE "$mariadatabase";
CREATE USER "$mariauser"@'localhost' IDENTIFIED BY "$mariapasswd";
GRANT ALL ON "$mariadatabase".* TO "$mariauser"@'localhost';
FLUSH PRIVILEGES;
EOT     

# Installation de Nextcloud
wget https://download.nextcloud.com/server/releases/nextcloud-17.0.1.zip

# Extraction
unzip nextcloud-17.0.1.zip
cp -R nextcloud /var/www/html/
chown -R www-data:www-data /var/www/html/nextcloud/

# Configuration du serveur web
sed -i 's/DocumentRoot\ \/var\/www\/html/DocumentRoot\/var\/www\/html\/nextcloud/' /etc/apache2/sites-available/default-ssl.conf

a2enmod rewrite
a2enmod headers
a2enmod ssl
a2ensite default-ssl
service apache2 restart

# Résoudre les alertes
sed -i '172s/AllowOverride\ None/AllowOverride\ All/' /etc/apache2/apache2.conf
service apache2 restart

# Enabling MySQL 4-byte support
#https://docs.nextcloud.com/server/17/admin_manual/configuration_database/mysql_4byte_support.html
#SET GLOBAL innodb_file_format=Barracuda;
#SET GLOBAL innodb_large_prefix=ON;
#systemctl restart mariadb
#ALTER  DATABASE  nextcloud  CHARACTER  SET  utf8mb4  COLLATE  utf8mb4_general_ci ;
#cd /var/www/html/nextcloud
#sudo -u www-data php occ config:system:set mysql.utf8mb4 --type boolean --value="true"

echo -e " \n Connecter vous sur https://192.168.0.26 pour continuer l'installation ! \n"
echo -e "Identifiants de la Base de données"
echo -e "Utilisateur de la base de donnée : "
echo -e "Mot de passe de la base de donnée : "
echo -e "Nom de la base de donnée : \n"
