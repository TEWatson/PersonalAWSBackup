#!/bin/bash
# This script zips up the files specified in paths.backup and runs AWSBackup.jar,
# which stores the .zip an AWS S3 bucket (org-watsont-taylor-backup)

# Delete the current backup folder
rm -rf $PWD/backup
rm $PWD/backup.zip

# Copy all the requested files locally
while read p; do
    echo "Making a local copy of $p..."
    cp -r $p $PWD/backup
done < paths.backup

# Zip the local copies up
echo "Zipping up the files..."
zip -r backup.zip $PWD/backup

# Send them off to AWS S3 with java
echo "Ready to store, calling Java connection delegate..."
java -jar AWSBackup.jar $PWD/backup.zip

exit
