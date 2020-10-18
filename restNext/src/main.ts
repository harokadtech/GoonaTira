// typescript
import Client, { Server, File, Folder, Tag, IQuota } from "nextcloud-node-client";

	
/* curl -I -k -X PROPFIND -H "Depth: 1" -u TestCadastre:TestCadastre123 https://localhost/remote.php/dav/files/TestCadastre/ 
 curl -I -k  -u TestCadastre:TestCadastre123 -X MKCOL "https://localhost/remote.php/dav/files/TestCadastre/TestCurl"
  */
(async () => {
try {
	const readline = require('readline-sync');
	var userinput = 0;
	process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
    // create a new client using connectivity information from environment 
	// export NEXTCLOUD_PASSWORD=*******
	const mypassword = process.env.NEXTCLOUD_PASSWORD
	const server: Server = new Server(
		{ basicAuth:
			{ 
			  username: "TestCadastre",
			  password: mypassword,
			},
			url: "https://194-146-38-32.cloud-xip.io/remote.php/dav/files/TestCadastre/",
		});
	// Override WebDAV URL pattern
    Client.webDavUrlPath = "/remote.php/dav";				
	const client = new Client(server);
	console.log('connected :\n');
	const q: IQuota = await client.getQuota(); 
	console.log("Quota :" + q.available + '\n');
	//const existingfile = await client.getFile("Dossier_TF_test/BordereauAnalytique.docx");
	// create a folder structure
	// current date
	let myDate = new Date();
	// adjust 0 before single digit date
	let date = ("0" + myDate.getDate()).slice(-2);
	// current month
	let month = ("0" + (myDate.getMonth() + 1)).slice(-2);
	const suffix = myDate.getFullYear() +'_'+ month +'_'+ date+'_'+ myDate.getHours() +'_'+ myDate.getMinutes();
	const folder: Folder = await client.createFolder("DossierTF_"+ suffix);
	userinput = readline.question('Type ENTER to continue steps :\n');
	// create file within the folder
	const newfile: File = await folder.createFile("MaDemande" + suffix + ".txt", Buffer.from("Titre foncier test file content"));
	userinput = readline.question('Type ENTER to continue steps :\n');
	// get the file content
	const content: Buffer = await newfile.getContent();
	console.log('content :\n' + content);
    const url = await newfile.getUrl();
	// get the file URL
	console.log("Got url : " + url);

	// add a tag to the file and create the tag if not existing
	await newfile.addTag("Nouveau");
	// add a comment to the file
	
	await newfile.addComment("Nouvelle demande immatriculation TF");
	//const shareLink:string = share.url;
	// delete the folder including the file 
	//await folder.delete();

		
} catch (e) {
	// some error handling   
	console.log(e);
}
})();

// Env variables
//export NEXTCLOUD_USERNAME= "<your user name>"
//export NEXTCLOUD_PASSWORD = "<your password>"
//export NEXTCLOUD_URL= "https://194-146-38-32.cloud-xip.io/remote.php/dav/files/TestCadastre/"