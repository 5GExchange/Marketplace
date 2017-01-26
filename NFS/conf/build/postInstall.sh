#! /bin/bash

echo "post install"

[ -d /etc/rc0.d ] && ln -sf /etc/init.d/nfs /etc/rc0.d/K01nfs 
[ -d /etc/rc1.d ] && ln -sf /etc/init.d/nfs /etc/rc1.d/S99nfs
[ -d /etc/rc2.d ] && ln -sf /etc/init.d/nfs /etc/rc2.d/S99nfs
[ -d /etc/rc3.d ] && ln -sf /etc/init.d/nfs /etc/rc3.d/S99nfs
[ -d /etc/rc4.d ] && ln -sf /etc/init.d/nfs /etc/rc4.d/S99nfs
[ -d /etc/rc5.d ] && ln -sf /etc/init.d/nfs /etc/rc5.d/S99nfs
[ -d /etc/rc6.d ] && ln -sf /etc/init.d/nfs /etc/rc6.d/K01nfs
