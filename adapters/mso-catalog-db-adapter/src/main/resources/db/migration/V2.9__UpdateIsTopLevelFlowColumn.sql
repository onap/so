USE catalogdb;

ALTER TABLE
  `northbound_request_ref_lookup` CHANGE ISTOPLEVELFLOW IS_TOPLEVELFLOW TINYINT(1);