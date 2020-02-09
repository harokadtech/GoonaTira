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


