import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {
	title="2019 Editeur de rapports de statistiques réseaux (CRUD) | développé pour Atos";
	constructor() { }

  	ngOnInit() {
  	}

}
