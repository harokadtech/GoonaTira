#!/bin/bash

#Install Nextcloud 17 sur ubuntu 18.04
#https://www.c-rieger.de/nextcloud-installation-guide-ubuntu-18-04/
#Conteneur Proxmox: Ubuntu-18.04-standard_18.04.1-1_amd64.tar.gz

# Variables
vernext="18.0.0"
verphp="7.3"
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
" >> /var/www/html/nextcloud/config/config.php

echo 'systemctl restart apache2'
systemctl restart apache2