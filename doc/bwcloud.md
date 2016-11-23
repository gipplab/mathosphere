# This file describes the installation at bwcould

We have three vm
* [master (m)](https://bwcloud.ruf.uni-freiburg.de/dashboard/project/instances/59ed6501-fb62-4bbf-b2c4-b3b92665506e/)
IP 192.168.0.3, HDFS namenode, flink job manager
* [worker-1 (w1)](https://bwcloud.ruf.uni-freiburg.de/dashboard/project/instances/eec808f5-2603-4d1a-8bb6-4fb82ceb96ef/)
IP 192.168.0.5, HDFS datanode, flink task manager
* [worker-2 (w2)](https://bwcloud.ruf.uni-freiburg.de/dashboard/project/instances/447dfc68-2210-42ad-86d9-8abe00ebe15c/)
IP 192.168.0.4, HDFS datanode, flink task manager

and one [virtual network](https://bwcloud.ruf.uni-freiburg.de/dashboard/project/networks/2de311c0-0cec-4733-a022-d166eb175dfd/detail)
in the IP range 192.168.0.0/24.

## Prerequisites
Install Java8
```
sudo apt-get install openjdk-8-jdk
```
Install flink
```
wget http://mirror.switch.ch/mirror/apache/dist/flink/flink-1.1.2/flink-1.1.2-bin-hadoop27-scala_2.10.tgz
tar xzf flink-*.tgz
mv flink-*/ flink
sudo mkdir /srv
sudo chown ubuntu /srv
mv flink /srv/
```
Install hdfs
```
wget http://mirror.switch.ch/mirror/apache/dist/hadoop/common/hadoop-2.7.3/hadoop-2.7.3.tar.gz
tar -xf hadoop-*.tar.gz
mv hadoop-* /srv/hadoop
```
Setup ssh self access
```
ssh-keygen
cat id_rsa.pub >> authorized_keys
```
and copy the ssh key to the works in authorized_keys (manually).

## Configuration
See [config files](./bwcloud/cfg) especially the hosts file is important.
To deploy the config there is a [script](./bwcloud/scr/deploy-cfg).

## Security
Access via RDP was denied.

## Data

### NTCIR-12 arXiv
```
wget --user ntcir12mathir --ask-password http://ntcir-math.nii.ac.jp/download/NTCIR_2014_dataset/NTCIR_2014_dataset_XHTML5.tar.gz
tar -xf NTCIR_2014_dataset_XHTML5.tar.gz
../scripts/convert ./xhtml5 /tmp 100 ntcir12.xml

```
To get a password ask here http://ntcir-math.nii.ac.jp/


## Running a WC Flink job

### From the CLI
On m execute
```bash
ubuntu@master:/srv/flink$ ./bin/flink run ./examples/batch/WordCount.jar --input hdfs://m:54310/data/ntcir/12/arxiv/xhtml/together.xml --output hdfs://m:54310/out/wc
```

## To be sorted
Copy hosts
```
cp /etc/hosts w1:~
scp /etc/hosts w2:~
ssh w1 'sudo mv hosts /etc'
ssh w2 'sudo mv hosts /etc'

```
Check which java tasks are running
```
jps
```
Start and stop flink web-interface at port [8081](http://localhost:8081)
```
/srv/flink/bin/stop-cluster.sh
/srv/flink/bin/stop-local.sh
/srv/flink/bin/start-cluster.sh
```
set latest java home
```
export JAVA_HOME=$(readlink -f /usr/bin/java | sed "s:bin/java::")
```

ipconfig
```
ifconfig eth1
ifconfig eth0
sudo dhclient -v eth0
sudo dhclient -v eth1
sudo dhclient -v eth2
ifconfig
ping 192.168.0.4
ping 192.168.0.5
ping 192.168.0.4
```
Get disk partitioning info
```
sudo sfdisk -d /dev/vdb > dsk.part
```
Apply disk creation scripts
```
./frmt-dsk
./foreachdatadisk 'mkdir tmp'
./foreachdatadisk 'mkdir hdfs'
```
format namenode
```
/srv/hadoop/bin/hadoop namenode -format
/srv/hadoop/sbin/start-dfs.sh
```

Install htop
```
sudo apt-get install htop
```
Install crashplan
```
wget https://download.code42.com/installs/linux/install/CrashPlan/CrashPlan_4.7.0_Linux.tgz
tar -xf CrashPlan_4.7.0_Linux.tgz
cd crashplan-install/
./install.sh
cat /var/lib/crashplan/.ui_info
cd /usr/local/crashplan
cd bin/
./CrashPlanEngine status
cd ..
cd conf/
ls
netstat -na | grep LISTEN | grep 42
cat /var/lib/crashplan/.ui_info
mkdir /data2/crash
./CrashPlanEngine stop
sudo mv cache/* /data2/crash/
sudo vi /usr/local/crashplan/conf/my.service.xml
./CrashPlanEngine start
```
Convert corpus
```
cd /data
../scripts/convert ./xhtml5 /tmp 100 ntcir12.xml
exit
```
Create hdfs folder
```
/srv/hadoop/bin/hadoop fs mkdir /data
/srv/hadoop/bin/hadoop fs -mkdir /data
/srv/hadoop/bin/hadoop fs -mkdir -p /data/ntcir/12/arxiv/xhtml
/srv/hadoop/bin/hadoop fs -copyFromLocal /data/ntcir/ntcir12.xml /data/ntcir/12/arxiv/xhtml/together.xml
```
HDFS web ui port [50070](http://localhost:50070)


format disk (as root)
```
fdisk /dev/vdc
```
see m for commands... we create a **n**ew **p**rimary partition
 and **w**rite that info to disk before we **q**uit.
```
mkfs.ext4 /dev/vdc1
vi /etc/fstab
sfdisk -d /dev/vdc
```

my local .ssh/config file
```
Host m
    HostName master.mediabotz.de
    User ubuntu
    LocalForward 4200 127.0.0.1:4243
    LocalForward 8081 127.0.0.1:8081
    LocalForward 50070 127.0.0.1:50070
```
