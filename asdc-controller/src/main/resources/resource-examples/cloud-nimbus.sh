#!/bin/bash
echo "Running first-boot script"
FLAG="first-boot.sh"
echo "First boot run" > ${FLAG}
echo "$vm_name" >> ${FLAG}
touch /var/lib/cloud/instance/payload/launch-params
chmod 644 /var/lib/cloud/instance/payload/launch-params
#for i in $(ls /sys/class/net); do
#  echo "Restart $i" >> ${FLAG}
#  ifdown ${i}
#  ifup ${i}
#done
