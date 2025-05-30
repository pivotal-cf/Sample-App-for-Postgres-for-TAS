# Sample App for Postgres for TAS

This repostory contains:

1. Sample Cloud foundry Java Spring Webapp
2. BBR restore script for restoring HA deployment's online S3 backup artifacts to any HA Postgres deployment


## How to use the sample application

### Pre-requisites

* Java 8
* Maven 3.0 or newer
* Cloud Foundry CLI

### 1. Build the application

To build the application, run the following command from the `postgres-consumer-app` directory:

```
mvn clean install && mv target/postgres-demo-0.0.1-SNAPSHOT.jar ./
```

### 2. Deploy and bind the application

To deploy the application, first edit the `APP_NAME`,`SVC_INSTANCE_NAME` and `SSL_MODE` in the `postgres-consumer-app/manifest.yml` file.
The `SSL_MODE` values are listed [here](https://www.postgresql.org/docs/8.4/libpq-connect.html#LIBPQ-CONNECT-SSLMODE).
Then run the following command from the `postgres-consumer-app` directory:
```
cf push
```
This will create, push and bind the application to the service instance you mentioned in `manifest.yaml` file.


## How to use the BBR restore script: ha-bbr-restore.sh

This script is useful in only [this case](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-for-postgres-on-cloud-foundry/10-0/postgres/backup-restore-bbr.html#restore). Although running it in any other case will also work, it will be simply a regular BBR restore.

The backup artifacts from HA service instance need a modification to be used with bbr restore. As you can see in the [directory structure for HA backup artifacts](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-for-postgres-on-cloud-foundry/10-0/postgres/backup-restore-bbr.html#structure), there are 3 metadata files generated and saved, 1 for each postgres instance. To start your restore, first download the whole folder corresponding to your HA service deployment, as described in the structure section. BBR needs only 1 metadata file to be used during restore, which is essentially a union of all 3 files. The following script does all of this. 

Place the file `ha-bbr-restore.sh` inside the top level `service-instance_` folder downloaded from your configured S3 bucket for backups. After this, only keep the timetamp folders that you want. You can delete all other timestamp folders from all VM instance folders. Then:

```
cp ./ha-bbr-restore.sh downloaded-s3-backup-folder/
cd downloaded-s3-backup-folder/
./ha-bbr-restore.sh
```

After creating necessary files, this will run [regular BBR restore](https://docs.cloudfoundry.org/bbr/index.html#workflow-restore).
No additional steps need to be taken.

## How to use the BBR restore script for data migration from v1.2.x to v10.0.0 for HA set up: ha-bbr-restore-migration.sh

This script is useful in only [restoring HA service instance](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-for-postgres-on-cloud-foundry/10-0/postgres/backup-restore-bbr.html#restore-frm-bkup-artfts).

The backup artifacts from HA service instance need a modification to be used with bbr restore. As you can see in the [directory structure for HA backup artifacts](https://techdocs.broadcom.com/us/en/vmware-tanzu/data-solutions/tanzu-for-postgres-on-cloud-foundry/1-2/postgres-1-2/backup-restore-bbr.html#structure), there are 3 metadata files generated and saved, 1 for each postgres instance. To start your restore, first download the whole folder corresponding to your HA service deployment, as described in the structure section. BBR needs only 1 metadata file to be used during restore, which is essentially a union of all 3 files. The following script does all of this.

Place the file `ha-bbr-restore-migration.sh` inside the top level `downloaded-s3-backup-folder` downloaded from your configured S3 bucket for backups. Then:

```
cp ./ha-bbr-restore-migration.sh downloaded-s3-backup-folder/
cd downloaded-s3-backup-folder/
./ha-bbr-restore-migration.sh <service-instance-RESTORE_SERVICE_ID>
```

After creating necessary files, this script will run [regular BBR restore](https://docs.cloudfoundry.org/bbr/index.html#workflow-restore).
No additional steps need to be taken.
