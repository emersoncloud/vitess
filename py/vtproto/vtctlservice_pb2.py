# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: vtctlservice.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


import vtctldata_pb2 as vtctldata__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='vtctlservice.proto',
  package='vtctlservice',
  syntax='proto3',
  serialized_options=_b('Z)vitess.io/vitess/go/vt/proto/vtctlservice'),
  serialized_pb=_b('\n\x12vtctlservice.proto\x12\x0cvtctlservice\x1a\x0fvtctldata.proto2q\n\x05Vtctl\x12h\n\x13\x45xecuteVtctlCommand\x12%.vtctldata.ExecuteVtctlCommandRequest\x1a&.vtctldata.ExecuteVtctlCommandResponse\"\x00\x30\x01\x42+Z)vitess.io/vitess/go/vt/proto/vtctlserviceb\x06proto3')
  ,
  dependencies=[vtctldata__pb2.DESCRIPTOR,])



_sym_db.RegisterFileDescriptor(DESCRIPTOR)


DESCRIPTOR._options = None

_VTCTL = _descriptor.ServiceDescriptor(
  name='Vtctl',
  full_name='vtctlservice.Vtctl',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  serialized_start=53,
  serialized_end=166,
  methods=[
  _descriptor.MethodDescriptor(
    name='ExecuteVtctlCommand',
    full_name='vtctlservice.Vtctl.ExecuteVtctlCommand',
    index=0,
    containing_service=None,
    input_type=vtctldata__pb2._EXECUTEVTCTLCOMMANDREQUEST,
    output_type=vtctldata__pb2._EXECUTEVTCTLCOMMANDRESPONSE,
    serialized_options=None,
  ),
])
_sym_db.RegisterServiceDescriptor(_VTCTL)

DESCRIPTOR.services_by_name['Vtctl'] = _VTCTL

# @@protoc_insertion_point(module_scope)
