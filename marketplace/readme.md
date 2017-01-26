# Marketplace Deployment Info

## Docker installation info

```sh
$ sudo apt-get update
$ sudo apt-get install -y apt-transport-https ca-certificates python-pip
$ sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
$ echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee -a /etc/apt/sources.list.d/docker.list
$ sudo apt-get update
$ sudo apt-get install -y linux-image-extra-$(uname -r)
$ sudo apt-get update
$ sudo apt-get install -y docker-engine
$ sudo service docker start
$ sudo apt-get upgrade -y
$ sudo pip install docker-compose
```

## Deployment

```sh
$ cd marketplace
$ sudo docker-compose up
```

Visit http://localhost/
