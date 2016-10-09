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
wget --user ntcir12mathir --ask-password http://ntcir-math.nii.ac.jp/download/NTCIR_2014_dataset/NTCIR_2014_dataset_HTML5.tar.gz
```
To get a password ask here http://ntcir-math.nii.ac.jp/

