import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ErnoComponent } from './erno.component';

describe('ErnoComponent', () => {
  let component: ErnoComponent;
  let fixture: ComponentFixture<ErnoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ErnoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ErnoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
