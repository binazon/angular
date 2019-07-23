import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HautIndexComponent } from './haut-index/haut-index.component';
import { FooterComponent } from './footer/footer.component';
import { CreerOffreFormulaireComponent } from './creer-offre-formulaire/creer-offre-formulaire.component';
import { ListeOffreComponent } from './liste-offre/liste-offre.component';
import { ErnoComponent } from './erno/erno.component';
import { StepperComponent } from './stepper/stepper.component';

const routes: Routes = [
  { path: '', pathMatch:'full', redirectTo: 'listeOffre' },
  { path: 'bas', component: FooterComponent },
  { path: 'haut', component: HautIndexComponent },
  { path: 'offreFormulaire', component: CreerOffreFormulaireComponent },
  { path: 'listeOffre', component: ListeOffreComponent },
  { path: 'listeOffreOption', component: StepperComponent },
  /*{ path: 'reportConfig', children: [
    { path: '', redirectTo: 'erno', pathMatch: 'full' },
    { path: ':element', component: ReportConfigComponent },
    { path: 'erno', component: ErnoComponent }
  ]},*/
  { path: '**', pathMatch:'full', redirectTo: 'listeOffre' }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {
  static components = [
    FooterComponent,
    HautIndexComponent,
    CreerOffreFormulaireComponent,
    ListeOffreComponent,
    ErnoComponent,
    StepperComponent
  ];
}
