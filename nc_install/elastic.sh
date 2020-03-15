#!/bin/bash
sudo apt install openjdk-11-jdk -y
sudo apt-get install openjdk-11-jdk apt-transport-https -y
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
# Add a repository to install latest Elasticsearch 7.X on your Ubuntu system.
add-apt-repository "deb https://artifacts.elastic.co/packages/7.x/apt stable main"
# install Elasticsearch packages on your system.
sudo apt-get update
sudo apt-get install elasticsearch -y

cp -vf el_config.yml /etc/elasticsearch/elasticsearch.yml

sleep 2

sudo ufw allow 8228
sudo ufw enable -y
sudo ufw status

sudo /bin/systemctl enable elasticsearch.service
# Elasticsearch can be started and stopped as follows:

sudo systemctl start elasticsearch.service

sleep 2
curl -X GET "http://localhost:8228/?pretty"

curl -X PUT "http://localhost:8228/test" -d '{
  "settings": {
    "index": {
      "number_of_replicas": 0,
      "number_of_shards": 1
    }
  }
}'

"http://localhost:8228/?pretty"
curl -H'Content-Type: application/json' -XPOST localhost:9200/exampleindex/doc/1 -d '{ "message": "this the value for the message field" }'


sudo -u www-data php ./occ fulltextsearch:index

sleep 2
#sudo systemctl stop elasticsearch.service
#sudo systemctl restart elasticsearch
exit 0

# Download latest release from Github (insert copied link)
wget https://github.com/cdr/code-server/releases/download/2.1698/code-server2.1698-vsc1.41.1-linux-x86_64.tar.gz
# Unpack tarball
tar -xvzf code-server2.1698-vsc1.41.1-linux-x86_64.tar.gz
# Run Code Server
cd code-server2.1698-vsc1.41.1-linux-x86_64
cp code-server /usr/bin/
cd ..
./code-server

sudo apt-get install -y maven



# node install
curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
sudo apt-get  install nodejs -y


