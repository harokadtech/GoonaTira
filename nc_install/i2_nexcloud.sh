#!/bin/bash

#Install Nextcloud 17 sur ubuntu 18.04
#https://www.c-rieger.de/nextcloud-installation-guide-ubuntu-18-04/
#Conteneur Proxmox: Ubuntu-18.04-standard_18.04.1-1_amd64.tar.gz

# Variables
vernext="18.0.0"
verphp="7.3"

# identiants mariadb pour nextcloud
mariadatabase="nextcloud"
mariauser="nclouduser"
mariapasswd="Wsxzaq123$"

ipnet=$(hostname -I | awk '{print $1}')
usertos=$(w | awk '{print $1}' | awk 'NR==3')


mysql -u root -p'digital' << EOT
CREATE USER 'nextdbuser'@'localhost' IDENTIFIED BY 'digital';
CREATE DATABASE IF NOT EXISTS nextcloud CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
GRANT ALL PRIVILEGES on nextcloud.* to 'nextdbuser'@'localhost';
FLUSH privileges;
EOT

echo 'Downloading nexcloud'
sleep 5
# Installation de Nextcloud
wget https://download.nextcloud.com/server/releases/nextcloud-18.0.0.zip

# Extraction
unzip nextcloud-18.0.0.zip
cp -R nextcloud /var/www/html/
chown -R www-data:www-data /var/www/html/nextcloud/

rm -R nextcloud
rm nextcloud-18.0.0.zip
# Configuration du serveur web
sed -i 's/DocumentRoot\ \/var\/www\/html/DocumentRoot\ \/var\/www\/html\/nextcloud/' /etc/apache2/sites-available/default-ssl.conf
a2enmod rewrite
a2enmod headers
a2enmod ssl
a2ensite default-ssl
service apache2 restart

# Résoudre les alertes
sed -i '172s/AllowOverride\ None/AllowOverride\ All/' /etc/apache2/apache2.conf
service apache2 restart

###
sed -i -e "/ServerAdmin/a \                \Header always set Strict-Transport-Security 'max-age=15552000; includeSubDomains'" /etc/apache2/sites-available/default-ssl.conf

### Memory caching
# Install and configure Redis
apt update
apt install redis-server -y
apt install php-redis -y
cp /etc/redis/redis.conf /etc/redis/redis.conf.save
sed -i "s/port 6379/port 0/" /etc/redis/redis.conf
sed -i s/\#\ unixsocket/\unixsocket/g /etc/redis/redis.conf
sed -i "s/unixsocketperm 700/unixsocketperm 770/" /etc/redis/redis.conf
sed -i "s/# maxclients 10000/maxclients 512/" /etc/redis/redis.conf
usermod -aG redis www-data

cp /etc/sysctl.conf /etc/sysctl.conf.save
sed -i '$avm.overcommit_memory = 1' /etc/sysctl.conf

echo -e " \n Connecter vous sur https://192.168.0.26 pour finaliser l'installation ! \n"
echo -e "Identifiants de la Base de données"
echo -e "Utilisateur de la base de donnée : "
echo -e "Mot de passe de la base de donnée : "
echo -e "Nom de la base de donnée : \n"
