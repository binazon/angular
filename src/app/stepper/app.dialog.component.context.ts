import { BSModalContext } from 'ngx-modialog/plugins/bootstrap';

export class AppDialogComponentContext extends BSModalContext {
  constructor() {
    super();
  }

  public message: string;
}
