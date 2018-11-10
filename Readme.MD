# Additional setup sc-docker

Create an internal docker network, or sc-docker will create a non-internal one: 
docker network create --internal --subnet 172.18.0.0/16 sc_net