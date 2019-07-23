import {Component, ElementRef, ViewChild, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-liste-offre',
  templateUrl: './liste-offre.component.html',
  styleUrls: ['./liste-offre.component.css']
})
export class ListeOffreComponent implements OnInit{
	title = "Offres du fichier offerAndOption";
	choix="Choisir le fichier offerAndOption";
	changer="Changer de fichier";
	form: FormGroup;
	loading: boolean = false;
	fileContent: string = '';
	
	@ViewChild('fileInput') fileInput: ElementRef;
	constructor(private fb: FormBuilder) {
  		this.createForm();
	}
	ngOnInit(){}
	createForm() {
			this.form = this.fb.group({
  			fileinput: ['', Validators.required]
		});
	}
	handleFileSelect(files : FileList) :void{
		var output = [];
		let file = files[0];
		//on parcours la liste des fichiers
		for (var i = 0, f; f = files[i]; i++) {
			//traitement si fichier offerAndOption
    		if(f.name != 'offerAndOption.xml')
      		output.push('<p>','Choisir le fichier offerAndOption.xml afin d\'afficher le contenu du fichier et commencer la configuration !','</p>');
    		else{
    			//affichage des informations du fichier
      			output.push('<li>', escape(f.name), ' (', f.type || 'n/a', ') - ',
      			f.size, ' bytes, last modified: ',
      			f.lastModifiedDate ? f.lastModifiedDate.toLocaleDateString() : 'n/a','</li>');
    		}
		}
		document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>';
	}
	clearFileContent(){
  		this.fileContent = null;
	}
	onFileChange(event){
		if(event.target.files.length > 0) {
			let file = event.target.files[0];
			this.form.get('fileinput').setValue(file);
		}
	}
	onSubmit() {
		let input = new FormData();
		input.append('fileinput', this.form.get('fileinput').value);
		const formModel = input;
		this.loading = true;
		setTimeout(() => {
  			this.loading = false;
		}, 1000);
	}
	clearFile(){
	  	this.form.get('fileinput').setValue(null);
	  	this.fileInput.nativeElement.value = '';
	  	this.clearFileContent();
	  	document.getElementById('list').innerHTML = null;
	  	document.getElementById('contenu1').innerHTML = '';
	}
	bodyAppend(tagName, innerHTML){
	  	var elm;
	  	elm = document.createElement(tagName);
	  	elm.innerHTML = innerHTML;
	  	document.body.appendChild(elm);
	}
	loadFile() : any{
		var contenu =[];
		var offre = [];
	  	var options = [];
		var input, file, fr;
		this.fileContent = "";
		input = document.getElementById('fileinput');
		if (!input)
	    	this.bodyAppend("p", "Ne peux trouver le fichier selectionné.");
		else if (!input.files) 
	    	this.bodyAppend("p", "Le fichier selectionné ne peux pas être importé.");
		else if (!input.files[0])
	    	this.bodyAppend("p", "Selectionnez le fichier avant de cliquer 'Load'");
		else {
		    document.getElementById("contenu1").innerHTML="";
	  		file = input.files[0];
	  		fr = new FileReader();
	  		var parser = new DOMParser();
	  		fr.onload = function(e){
	    		var taille, j;
	  			var doc = parser.parseFromString(e.target.result, "text/xml");
	  			var tableau = doc.getElementsByTagName("offer");
	  			for(var i=0; i<tableau.length; i++){
	  				offre.push("<strong>commercialName: </strong>"+doc.getElementsByTagName("offer")[i].getAttribute("commercialName")+" , <strong>alias: </strong>"+doc.getElementsByTagName("offer")[i].getAttribute("alias")+"\n");
	  				contenu.push(offre[i]+"");
		  		}
	        	document.getElementById("contenu1").innerHTML='<ul>'+contenu.join("")+'</ul>';
	  		}
	  		fr.readAsText(file);
		}
		return contenu;
  	}
}
