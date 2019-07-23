import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HautIndexComponent } from './haut-index.component';

describe('HautIndexComponent', () => {
  let component: HautIndexComponent;
  let fixture: ComponentFixture<HautIndexComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HautIndexComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HautIndexComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
