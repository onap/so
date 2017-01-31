#!/bin/bash

#. .config 
#following would be in the .config file
 device_name="/dev/vde"
 install_script="install_nimbus.sh"
 enable_logic_flag_file=".flag"
#end of config file

#get the semaphore, 0 - disbaled, 1- enabled
flag=$(cat ${enable_logic_flag_file})

#check if device is mounted already
test=$(mount | grep ${device_name})
if [ "$flag" == "1" ]; then
	if [ -e ${device_name} ] && [ ! "${test}" ]; then
		#mount the iso image
		mount -t iso9660 -v -o loop /dev/vde/ /mnt/iso
		#if availabe run the install script (it contains the install.sh steps)
		if [ -e "${install_script}" ] && [ -f "${install_script}" ]; then
			${install_script}
		fi
		#disable the script from attempting to 
		#  mount and run install again until needed;
		echo "0" > ${enable_logic_flag_file}
		#if nedeed add step to comment out the crontab line here;
	fi
else
	echo "Auto mounting ISO & run install logic disabled!"
fi

#cron job
# * * * * * /mount_iso_script.sh

