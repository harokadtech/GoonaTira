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

apt update
sleep 50
apt full-upgrade -y

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

# install MySQL

apt install mysql-server -y
service mysql restart

# Configuration de MySQL

echo "
[server]
skip-name-resolve
innodb_buffer_pool_size = 128M
innodb_buffer_pool_instances = 1
innodb_flush_log_at_trx_commit = 2
innodb_log_buffer_size = 32M
innodb_max_dirty_pages_pct = 90
query_cache_type = 1
query_cache_limit = 2M
query_cache_min_res_unit = 2k
query_cache_size = 64M
tmp_table_size= 64M
max_heap_table_size= 64M
slow-query-log = 1
slow-query-log-file = /var/log/mysql/slow.log
long_query_time = 1

[client-server]
!includedir /etc/mysql/conf.d/
!includedir /etc/mysql/mysql.conf.d/

[client]
default-character-set = utf8mb4


[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_general_ci
transaction_isolation = READ-COMMITTED
binlog_format = ROW
innodb_large_prefix=on
innodb_file_format=barracuda
innodb_file_per_table=1
" > /etc/mysql/my.cnf

mysql -u root -p'digital' << EOT
CREATE USER 'next'@'localhost' IDENTIFIED BY 'digital';
CREATE DATABASE IF NOT EXISTS nextcloud CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
GRANT ALL PRIVILEGES on nextcloud.* to 'next'@'localhost';
FLUSH privileges;
EOT

# Installation de Nextcloud
wget https://download.nextcloud.com/server/releases/nextcloud-18.0.0.zip

# Extraction
unzip nextcloud-18.0.0.zip
cp -R nextcloud /var/www/html/
chown -R www-data:www-data /var/www/html/nextcloud/

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

read -p "Une fois l'installation terminée merci de valider avec la touche entrée"

sed -i '/);/d' /var/www/html/nextcloud/config/config.php

echo "
'activity_expire_days' => 14,
'auth.bruteforce.protection.enabled' => true,
'blacklisted_files' => 
array (
0 => '.htaccess',
1 => 'Thumbs.db',
2 => 'thumbs.db',
),
'cron_log' => true,
'enable_previews' => true,
'enabledPreviewProviders' => 
array (
0 => 'OC\Preview\PNG',
1 => 'OC\Preview\JPEG',
2 => 'OC\Preview\GIF',
3 => 'OC\Preview\BMP',
4 => 'OC\Preview\XBitmap',
5 => 'OC\Preview\Movie',
6 => 'OC\Preview\PDF',
7 => 'OC\Preview\MP3',
8 => 'OC\Preview\TXT',
9 => 'OC\Preview\MarkDown',
),
'filesystem_check_changes' => 0,
'filelocking.enabled' => 'true',
'htaccess.RewriteBase' => '/',
'integrity.check.disabled' => false,
'knowledgebaseenabled' => false,
'logfile' => '/var/nc_data/nextcloud.log',
'loglevel' => 2,
'logtimezone' => 'Europe/Paris',
'log_rotate_size' => 104857600,
'maintenance' => false,
'memcache.local' => '\OC\Memcache\APCu',
'memcache.locking' => '\OC\Memcache\Redis',
'overwriteprotocol' => 'https',
'preview_max_x' => 1024,
'preview_max_y' => 768,
'preview_max_scale_factor' => 1,
'redis' => 
array (
'host' => '/var/run/redis/redis-server.sock',
'port' => 0,
'timeout' => 0.0,
),
'quota_include_external_storage' => false,
'share_folder' => '/Shares',
'skeletondirectory' => '',
'theme' => '',
'trashbin_retention_obligation' => 'auto, 7',
'updater.release.channel' => 'stable',
);
" >> /var/www/nextcloud/config/config.php

systemctl restart apache2

reboot now

