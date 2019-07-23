import { Component, OnInit } from '@angular/core';
import { DialogRef, ModalComponent } from 'ngx-modialog';
import { AppDialogComponentContext } from './app.dialog.component.context';
//import { bootstrap3Mode  } from 'ngx-modialog/plugins/bootstrap';
//bootstrap3Mode();

@Component({
    templateUrl: './app.dialog.component.html'
})
export class AppDialogComponent implements OnInit {
    title = 'Modal Open';
    public message: string;

    constructor(public dialog: DialogRef<AppDialogComponentContext>) {
    }

    ngOnInit(): void {
        this.dialog.context.dialogClass = 'modal-dialog modal-lg';
        this.message = this.dialog.context.message;
    }

    onClose(): void {
        this.dialog.close();
    }

    onOk(): void {
        this.dialog.close('Ok');
    }
}
