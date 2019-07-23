import { Component, OnInit, ViewChild, ElementRef, Inject, ViewContainerRef, TemplateRef } from '@angular/core';
import { NgxXml2jsonService } from 'ngx-xml2json';
import { FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { overlayConfigFactory } from 'ngx-modialog';
import { Modal } from 'ngx-modialog/plugins/bootstrap';
import { AppDialogComponentContext } from './app.dialog.component.context';
import { AppDialogComponent } from './app.dialog.component';
import * as jsontoxml from 'jsontoxml';
import * as xmlformater from 'xml-formatter';
import beautify from 'xml-beautifier';
import { writeFile } from 'write-file';
import { saveAs } from 'file-saver';
import find from 'find-file-up';
import path from 'path';

@Component({
  selector: 'app-stepper',
  templateUrl: './stepper.component.html',
  styleUrls: ['./stepper.component.css']
})
export class StepperComponent implements OnInit {
    form: FormGroup;
    file : File;
    title = 'Modal Sample App';
    chercher ="Rechercher par attributs d'offre";
    searchText;
    //motcle : string[] = ['searchText'];
    optionType : string[] = ['', 'Type', 'cpltAlias', 'Label'];
    objs: any = {};
    @ViewChild('fileInput') fileInput: ElementRef;
    constructor(private ngxXml2jsonService: NgxXml2jsonService, private fb: FormBuilder, public modal: Modal){
	this.createForm();
    }

    /*fromDir(startPath,filter) : any{
      //console.log('Starting from dir '+startPath+'/');
      if (!fs.existsSync(startPath)){
        console.log("no dir ",startPath);
        return;
      }
      var files=fs.readdirSync(startPath);
      for(var i=0;i<files.length;i++){
        var filename=path.join(startPath,files[i]);
        var stat = fs.lstatSync(filename);
        if (stat.isDirectory()){
            this.fromDir(filename,filter); //recurse
        }
        else if (filename.indexOf(filter)>=0) {
            //console.log('-- found: ',filename);
	    return files[i];
        };
      };
    };*/

    open(event){
      if(event.value == 'cpltAlias'){
	this.chercher = 'Rechercher par complément alias';
        //this.motcle ='searchText1';
        console.log('cpltAlias');
      }
      else if(event.value == 'Type'){
	this.chercher = 'Rechercher par Type';
        //this.motcle = 'searchText1';
	console.log('Type');
      }
      else if(event.value == 'Label'){
	this.chercher = 'Rechercher par Label';
        //this.motcle = 'searchText1';
	console.log('Label');
      }
      else{
        this.chercher = "Rechercher par attributs d'offre";
        //this.motcle = 'searchText1';
	console.log('par offre');
      }
    }	
    ngOnInit() {
        const xmltext = `<note><to>User</to><from>Library</from><heading>Message</heading><body>Some XML to convert to JSON!</body></note>`;
    }
    createForm(){
	this.form = this.fb.group({
  		//fileinput: ['', Validators.required]
	});
    }
    parseXml(xmltext: string) {
        const parser = new DOMParser();
        const xml = parser.parseFromString(xmltext, 'text/xml');
        const obj = this.ngxXml2jsonService.xmlToJson(xml);
	this.objs = obj;
	console.log(this.objs);
	//console.log(this.objs.listOffer.offer[0]["@attributes"].alias);
    }
    onFileChange(event) {
        if (event.target.files && event.target.files.length > 0) {
            const file = event.target.files[0];
            this.file = file;
        }
    }

    onSubmit() {
        const reader = new FileReader();
        //const filePath : string = find.sync('offerAndOption.xml', './openstat-parent/arbrrt/src/main/resources/global/provisioning',2);
	//console.log(filePath);
        reader.readAsText(this.file);
        reader.onload = (p:any) => {
            this.parseXml(p.target.result);
        };
    }
    clearFile() {
        this.fileInput.nativeElement.value = '';
    }

    deleteOffre(data: Object){ 
	var tab : Array<Object> = new Array<Object>(0);
	const aliasObject = data["@attributes"].alias;
	for(var i=0;i<this.objs.listOffer.offer.length; i++){
		if(this.objs.listOffer.offer[i]["@attributes"].alias == data["@attributes"].alias){
			continue;
		}
		else{
			tab.push(this.objs.listOffer.offer[i]);
		}
	}
	this.objs = tab as Object;
	console.log(this.objs);
	const xml = jsontoxml(this.objs, {escape:false, xmlHeader:true});
	//const xml1 =xmlformater(xml);
	const xml2 = beautify(xml);
	console.log(xml2);

	//ecrire le xml en dur.

	let blob = new Blob([xml2], {type: "text/plain;charset=utf-8"});
      	saveAs(blob, './fido.txt');
      
      	/*writeFile('./fido.txt', 'hi', function (err) {
  	  if (err) return console.log(err)
  	  console.log('file is written')
	});*/


   
    }

    updateOffre(data: Object) {
	console.log("test1");
    }
    ajouterOffre(){
    }




    modalAlert() {
        const dialogRef = this.modal.alert()
            .size('sm')
            .title('Modal Alert')
            .showClose(true)
            .body(`A simple alert dialog message.`)
            .open();

        dialogRef.result.then((result) => {
            if (result) {
                alert(`The result is: ${result}`);
            }
        }, () => {
            return;
        });
    }

    modalConfirm(): void {
        const dialogRef = this.modal.confirm()
            .title('Modal Confirm')
            .cancelBtn('Cancel')
            .cancelBtnClass('btn btn-secondary')
            .okBtn('Ok')
            .okBtnClass('btn btn-primary')
            .isBlocking(true)
            .showClose(false)
            .body(`<p>A simple confirm dialog message.</p>`)
            .open();

        dialogRef.result.then((result) => {
            if (result) {
                alert(`The result is: ${result}`);
            }
        }, () => {
            return;
        });
    }

    modalOpen() {
        const componentContext = <AppDialogComponentContext>{ message: 'Hello Modal' };
        const dialogRef = this.modal.open(AppDialogComponent, overlayConfigFactory(componentContext));

        dialogRef.result.then((result) => {
            if (result) {
                alert(`The result is: ${result}`);
            }
        }, () => {
            return;
        });
    }
}
