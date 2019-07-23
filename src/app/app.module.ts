import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpModule } from "@angular/http";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";
import { HttpClientModule } from '@angular/common/http';
import { enableProdMode } from '@angular/core';
import { MatNativeDateModule } from '@angular/material/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { bootstrap } from "bootstrap";
import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DemoMaterialModule } from '../material-module';
import { Ng2SearchPipeModule } from 'ng2-search-filter';
import { ModalModule } from 'ngx-modialog';
import { BootstrapModalModule } from 'ngx-modialog/plugins/bootstrap';
import { AppDialogComponent } from './stepper/app.dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    AppRoutingModule.components,
    AppDialogComponent
  ],
imports: [
    BrowserModule,
    ReactiveFormsModule,
    HttpModule,
    AppRoutingModule,
    FormsModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    DemoMaterialModule,
    MatNativeDateModule,
    ReactiveFormsModule,
    ModalModule.forRoot(),
    BootstrapModalModule,
    Ng2SearchPipeModule
  ],
  providers: [],
  bootstrap: [AppComponent],
  entryComponents: [
    AppDialogComponent
  ]
})
export class AppModule {}
