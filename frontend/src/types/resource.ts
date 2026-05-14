export type ResourceType = 'functions' | 'menus' | 'messageResources';

export interface FunctionResource {
  id: number;
  aClass: string; bClass: string; cClass: string;
  action: string; functionName: string; functionId: string; type: string;
  light: boolean; standard: boolean; enterprise: boolean; systemMenu: boolean;
  productDomain: string; domainLicenseResourceType: string; relatedServices: string;
  resourceKey: string;
  rowOrder: number;
}

export interface MenuResource {
  id: number;
  mainMenu: string; subMenuGroup: string; subMenu: string;
  menuLevel1: string; menuLevel2: string; menuLevel3: string;
  menuId: string; isMenu: boolean; isSystemMenu: boolean;
  functionId: string; functionDescription: string; menuIcon: string;
  rowOrder: number;
}

export interface MessageResourceItem {
  id: number;
  rowNumber: number | null;
  duplicateStatus: string | null;
  module: string; resourceKey: string; fullResourceKey: string | null;
  korean: string; english: string; japanese: string;
  description: string; registeredDate: string; registeredBy: string;
  resourceKeyCount: number | null;
  koreanCount: number | null; englishCount: number | null; japaneseCount: number | null;
  rowOrder: number;
}

export const ACTION_OPTIONS = [
  'cmm.view', 'cmm.insert', 'cmm.delete', 'cmm.update',
  'cmm.upload', 'cmm.download_excel', 'download', 'cmm.excution', 'cmm.copy', 'cmm.load_1'
];

export const TYPE_OPTIONS = ['읽기', '쓰기', '실행', 'Excel', 'Import', 'Export'];

export const BOOLEAN_OPTIONS = ['TRUE', 'FALSE'];
