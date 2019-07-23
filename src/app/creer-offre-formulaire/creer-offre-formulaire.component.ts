import { Component, OnInit } from '@angular/core';
@Component({
	selector: 'app-creer-offre-formulaire',
	templateUrl: './creer-offre-formulaire.component.html',
	styleUrls: ['./creer-offre-formulaire.component.css']
})
export class CreerOffreFormulaireComponent implements OnInit {
	title = "Créer une nouvelle offre :";
  	constructor() {}
	ngOnInit() {}
  	ajouteOption(){
		var clone = document.getElementById("cloner").cloneNode(true);
		document.getElementById("tab").appendChild(clone);
  	}
}
