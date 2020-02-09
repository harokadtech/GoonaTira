// this must be the first
/*import { config } from "dotenv";
config();

import debugFactory from "debug";
const debug = debugFactory("xxx");
debug("xx");

import { CopyJob, ICopyJobMemento, NextcloudClient } from "./copyJob";

(async () => {
  // const job = new CopyJob({ fileFilter: ["*.css"] });
  const client: NextcloudClient = new NextcloudClient();
  const options: ICopyJobMemento = {
    targetRootFolder: "/copyTest",
    nextcloudClient: client,
    fileFilter: ["*.ts"],
    sourceRootFolder: "src",
  };

  const job = new CopyJob(options);
  await job.start();

})();
*/

// typescript
import Client, { Server, File, Folder, Tag, IQuota } from "nextcloud-node-client";

	
/* curl -I -k -X PROPFIND -H "Depth: 1" -u ncloudpermis:ncloudpermis123 https://localhost/remote.php/dav/files/ncloudpermis/Documents/ 
 curl -I -k  -u ncloudpermis:ncloudpermis123 -X MKCOL "https://localhost/remote.php/dav/files/ncloudpermis/TestCurl"
  */
(async () => {
    try {
const readline = require('readline-sync');
var userinput = 0;
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
    // And we can get the content back from the new location
//export NEXTCLOUD_USERNAME= "<your user name>"
//export NEXTCLOUD_PASSWORD = "<your password>"
//export NEXTCLOUD_URL= "https://ncloud.harokad.com/remote.php/dav/files/ncloudpermis/"
        // create a new client using connectivity information from environment 
	  const server: Server = new Server(
				{ basicAuth:
					{ 
					  username: "ncloudpermis",
					  password: "ncloudpermis123",
					},
					url: "https://185.237.96.246/remote.php/dav/files/ncloudpermis/",
				});
         Client.webDavUrlPath = "/remote.php/dav";				
	    const client = new Client(server);
		console.log('connected :\n');
		//const q: IQuota = await client.getQuota(); 
		 //console.log("Quota" + q);
		 //userinput = readline.question('Step :\n');
	const file1 = await client.getFile("/Documents/Example.md");
    const url1 = await file1.getUrl();
	 console.log("Got url : " + url1);
	 userinput = readline.question('Step :\n');
	// userinput = readline.question('Step :\n');
        // create a folder structure if not available
        const folder: Folder = await client.createFolder("PermisRemote");
		userinput = readline.question('Step :\n');
        // create file within the folder
        const file: File = await folder.createFile("myFile.txt", Buffer.from("My file content"));
        // add a tag to the file and create the tag if not existing
        await file.addTag("MyTag");
        // add a comment to the file
        await file.addComment("myComment");
		userinput = readline.question('Step :\n');
        // get the file content
        const content: Buffer = await file.getContent();
        /*
		// share the file publicly with password and note
        const share: Share = await client.createShare({ fileSystemElement: file });
        await share.setPassword("some password");
        await share.setNote("some note\nnew line");
        // use the url to access the share 
        const shareLink:string = share.url;*/
		userinput = readline.question('Step :\n');
        // delete the folder including the file and share
        await folder.delete();
		userinput = readline.question('Step :\n');
		
    } catch (e) {
        // some error handling   
        console.log(e);
    }
})();