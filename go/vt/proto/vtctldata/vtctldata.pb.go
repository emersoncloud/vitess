// Code generated by protoc-gen-gogo. DO NOT EDIT.
// source: vtctldata.proto

package vtctldata

import (
	fmt "fmt"
	io "io"
	math "math"
	reflect "reflect"
	strings "strings"

	proto "github.com/gogo/protobuf/proto"
	logutil "vitess.io/vitess/go/vt/proto/logutil"
)

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.GoGoProtoPackageIsVersion2 // please upgrade the proto package

// ExecuteVtctlCommandRequest is the payload for ExecuteVtctlCommand.
// timeouts are in nanoseconds.
type ExecuteVtctlCommandRequest struct {
	Args          []string `protobuf:"bytes,1,rep,name=args,proto3" json:"args,omitempty"`
	ActionTimeout int64    `protobuf:"varint,2,opt,name=action_timeout,json=actionTimeout,proto3" json:"action_timeout,omitempty"`
}

func (m *ExecuteVtctlCommandRequest) Reset()      { *m = ExecuteVtctlCommandRequest{} }
func (*ExecuteVtctlCommandRequest) ProtoMessage() {}
func (*ExecuteVtctlCommandRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_f41247b323a1ab2e, []int{0}
}
func (m *ExecuteVtctlCommandRequest) XXX_Unmarshal(b []byte) error {
	return m.Unmarshal(b)
}
func (m *ExecuteVtctlCommandRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	if deterministic {
		return xxx_messageInfo_ExecuteVtctlCommandRequest.Marshal(b, m, deterministic)
	} else {
		b = b[:cap(b)]
		n, err := m.MarshalTo(b)
		if err != nil {
			return nil, err
		}
		return b[:n], nil
	}
}
func (m *ExecuteVtctlCommandRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ExecuteVtctlCommandRequest.Merge(m, src)
}
func (m *ExecuteVtctlCommandRequest) XXX_Size() int {
	return m.ProtoSize()
}
func (m *ExecuteVtctlCommandRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_ExecuteVtctlCommandRequest.DiscardUnknown(m)
}

var xxx_messageInfo_ExecuteVtctlCommandRequest proto.InternalMessageInfo

func (m *ExecuteVtctlCommandRequest) GetArgs() []string {
	if m != nil {
		return m.Args
	}
	return nil
}

func (m *ExecuteVtctlCommandRequest) GetActionTimeout() int64 {
	if m != nil {
		return m.ActionTimeout
	}
	return 0
}

// ExecuteVtctlCommandResponse is streamed back by ExecuteVtctlCommand.
type ExecuteVtctlCommandResponse struct {
	Event *logutil.Event `protobuf:"bytes,1,opt,name=event,proto3" json:"event,omitempty"`
}

func (m *ExecuteVtctlCommandResponse) Reset()      { *m = ExecuteVtctlCommandResponse{} }
func (*ExecuteVtctlCommandResponse) ProtoMessage() {}
func (*ExecuteVtctlCommandResponse) Descriptor() ([]byte, []int) {
	return fileDescriptor_f41247b323a1ab2e, []int{1}
}
func (m *ExecuteVtctlCommandResponse) XXX_Unmarshal(b []byte) error {
	return m.Unmarshal(b)
}
func (m *ExecuteVtctlCommandResponse) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	if deterministic {
		return xxx_messageInfo_ExecuteVtctlCommandResponse.Marshal(b, m, deterministic)
	} else {
		b = b[:cap(b)]
		n, err := m.MarshalTo(b)
		if err != nil {
			return nil, err
		}
		return b[:n], nil
	}
}
func (m *ExecuteVtctlCommandResponse) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ExecuteVtctlCommandResponse.Merge(m, src)
}
func (m *ExecuteVtctlCommandResponse) XXX_Size() int {
	return m.ProtoSize()
}
func (m *ExecuteVtctlCommandResponse) XXX_DiscardUnknown() {
	xxx_messageInfo_ExecuteVtctlCommandResponse.DiscardUnknown(m)
}

var xxx_messageInfo_ExecuteVtctlCommandResponse proto.InternalMessageInfo

func (m *ExecuteVtctlCommandResponse) GetEvent() *logutil.Event {
	if m != nil {
		return m.Event
	}
	return nil
}

func init() {
	proto.RegisterType((*ExecuteVtctlCommandRequest)(nil), "vtctldata.ExecuteVtctlCommandRequest")
	proto.RegisterType((*ExecuteVtctlCommandResponse)(nil), "vtctldata.ExecuteVtctlCommandResponse")
}

func init() { proto.RegisterFile("vtctldata.proto", fileDescriptor_f41247b323a1ab2e) }

var fileDescriptor_f41247b323a1ab2e = []byte{
	// 258 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xe2, 0xe2, 0x2f, 0x2b, 0x49, 0x2e,
	0xc9, 0x49, 0x49, 0x2c, 0x49, 0xd4, 0x2b, 0x28, 0xca, 0x2f, 0xc9, 0x17, 0xe2, 0x84, 0x0b, 0x48,
	0xf1, 0xe6, 0xe4, 0xa7, 0x97, 0x96, 0x64, 0xe6, 0x40, 0x64, 0x94, 0xc2, 0xb9, 0xa4, 0x5c, 0x2b,
	0x52, 0x93, 0x4b, 0x4b, 0x52, 0xc3, 0x40, 0x4a, 0x9c, 0xf3, 0x73, 0x73, 0x13, 0xf3, 0x52, 0x82,
	0x52, 0x0b, 0x4b, 0x53, 0x8b, 0x4b, 0x84, 0x84, 0xb8, 0x58, 0x12, 0x8b, 0xd2, 0x8b, 0x25, 0x18,
	0x15, 0x98, 0x35, 0x38, 0x83, 0xc0, 0x6c, 0x21, 0x55, 0x2e, 0xbe, 0xc4, 0xe4, 0x92, 0xcc, 0xfc,
	0xbc, 0xf8, 0x92, 0xcc, 0xdc, 0xd4, 0xfc, 0xd2, 0x12, 0x09, 0x26, 0x05, 0x46, 0x0d, 0xe6, 0x20,
	0x5e, 0x88, 0x68, 0x08, 0x44, 0x50, 0xc9, 0x99, 0x4b, 0x1a, 0xab, 0xc1, 0xc5, 0x05, 0xf9, 0x79,
	0xc5, 0xa9, 0x42, 0x2a, 0x5c, 0xac, 0xa9, 0x65, 0xa9, 0x79, 0x25, 0x12, 0x8c, 0x0a, 0x8c, 0x1a,
	0xdc, 0x46, 0x7c, 0x7a, 0x30, 0x67, 0xb9, 0x82, 0x44, 0x83, 0x20, 0x92, 0x4e, 0x31, 0x17, 0x1e,
	0xca, 0x31, 0xdc, 0x78, 0x28, 0xc7, 0xf0, 0xe1, 0xa1, 0x1c, 0x63, 0xc3, 0x23, 0x39, 0xc6, 0x15,
	0x8f, 0xe4, 0x18, 0x4f, 0x3c, 0x92, 0x63, 0xbc, 0xf0, 0x48, 0x8e, 0xf1, 0xc5, 0x23, 0x39, 0x86,
	0x0f, 0x8f, 0xe4, 0x18, 0x27, 0x3c, 0x96, 0x63, 0x58, 0xf0, 0x58, 0x8e, 0xf1, 0xc2, 0x63, 0x39,
	0x86, 0x1b, 0x8f, 0xe5, 0x18, 0xa2, 0xd4, 0xca, 0x32, 0x4b, 0x52, 0x8b, 0x8b, 0xf5, 0x32, 0xf3,
	0xf5, 0x21, 0x2c, 0xfd, 0xf4, 0x7c, 0xfd, 0xb2, 0x12, 0x7d, 0xb0, 0x8f, 0xf5, 0xe1, 0x41, 0x91,
	0xc4, 0x06, 0x16, 0x30, 0x06, 0x04, 0x00, 0x00, 0xff, 0xff, 0x0c, 0x70, 0xe8, 0x34, 0x2f, 0x01,
	0x00, 0x00,
}

func (this *ExecuteVtctlCommandRequest) Equal(that interface{}) bool {
	if that == nil {
		return this == nil
	}

	that1, ok := that.(*ExecuteVtctlCommandRequest)
	if !ok {
		that2, ok := that.(ExecuteVtctlCommandRequest)
		if ok {
			that1 = &that2
		} else {
			return false
		}
	}
	if that1 == nil {
		return this == nil
	} else if this == nil {
		return false
	}
	if len(this.Args) != len(that1.Args) {
		return false
	}
	for i := range this.Args {
		if this.Args[i] != that1.Args[i] {
			return false
		}
	}
	if this.ActionTimeout != that1.ActionTimeout {
		return false
	}
	return true
}
func (this *ExecuteVtctlCommandResponse) Equal(that interface{}) bool {
	if that == nil {
		return this == nil
	}

	that1, ok := that.(*ExecuteVtctlCommandResponse)
	if !ok {
		that2, ok := that.(ExecuteVtctlCommandResponse)
		if ok {
			that1 = &that2
		} else {
			return false
		}
	}
	if that1 == nil {
		return this == nil
	} else if this == nil {
		return false
	}
	if !this.Event.Equal(that1.Event) {
		return false
	}
	return true
}
func (this *ExecuteVtctlCommandRequest) GoString() string {
	if this == nil {
		return "nil"
	}
	s := make([]string, 0, 6)
	s = append(s, "&vtctldata.ExecuteVtctlCommandRequest{")
	s = append(s, "Args: "+fmt.Sprintf("%#v", this.Args)+",\n")
	s = append(s, "ActionTimeout: "+fmt.Sprintf("%#v", this.ActionTimeout)+",\n")
	s = append(s, "}")
	return strings.Join(s, "")
}
func (this *ExecuteVtctlCommandResponse) GoString() string {
	if this == nil {
		return "nil"
	}
	s := make([]string, 0, 5)
	s = append(s, "&vtctldata.ExecuteVtctlCommandResponse{")
	if this.Event != nil {
		s = append(s, "Event: "+fmt.Sprintf("%#v", this.Event)+",\n")
	}
	s = append(s, "}")
	return strings.Join(s, "")
}
func valueToGoStringVtctldata(v interface{}, typ string) string {
	rv := reflect.ValueOf(v)
	if rv.IsNil() {
		return "nil"
	}
	pv := reflect.Indirect(rv).Interface()
	return fmt.Sprintf("func(v %v) *%v { return &v } ( %#v )", typ, typ, pv)
}
func (m *ExecuteVtctlCommandRequest) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *ExecuteVtctlCommandRequest) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if len(m.Args) > 0 {
		for _, s := range m.Args {
			dAtA[i] = 0xa
			i++
			l = len(s)
			for l >= 1<<7 {
				dAtA[i] = uint8(uint64(l)&0x7f | 0x80)
				l >>= 7
				i++
			}
			dAtA[i] = uint8(l)
			i++
			i += copy(dAtA[i:], s)
		}
	}
	if m.ActionTimeout != 0 {
		dAtA[i] = 0x10
		i++
		i = encodeVarintVtctldata(dAtA, i, uint64(m.ActionTimeout))
	}
	return i, nil
}

func (m *ExecuteVtctlCommandResponse) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *ExecuteVtctlCommandResponse) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if m.Event != nil {
		dAtA[i] = 0xa
		i++
		i = encodeVarintVtctldata(dAtA, i, uint64(m.Event.ProtoSize()))
		n1, err1 := m.Event.MarshalTo(dAtA[i:])
		if err1 != nil {
			return 0, err1
		}
		i += n1
	}
	return i, nil
}

func encodeVarintVtctldata(dAtA []byte, offset int, v uint64) int {
	for v >= 1<<7 {
		dAtA[offset] = uint8(v&0x7f | 0x80)
		v >>= 7
		offset++
	}
	dAtA[offset] = uint8(v)
	return offset + 1
}
func (m *ExecuteVtctlCommandRequest) ProtoSize() (n int) {
	if m == nil {
		return 0
	}
	var l int
	_ = l
	if len(m.Args) > 0 {
		for _, s := range m.Args {
			l = len(s)
			n += 1 + l + sovVtctldata(uint64(l))
		}
	}
	if m.ActionTimeout != 0 {
		n += 1 + sovVtctldata(uint64(m.ActionTimeout))
	}
	return n
}

func (m *ExecuteVtctlCommandResponse) ProtoSize() (n int) {
	if m == nil {
		return 0
	}
	var l int
	_ = l
	if m.Event != nil {
		l = m.Event.ProtoSize()
		n += 1 + l + sovVtctldata(uint64(l))
	}
	return n
}

func sovVtctldata(x uint64) (n int) {
	for {
		n++
		x >>= 7
		if x == 0 {
			break
		}
	}
	return n
}
func sozVtctldata(x uint64) (n int) {
	return sovVtctldata(uint64((x << 1) ^ uint64((int64(x) >> 63))))
}
func (this *ExecuteVtctlCommandRequest) String() string {
	if this == nil {
		return "nil"
	}
	s := strings.Join([]string{`&ExecuteVtctlCommandRequest{`,
		`Args:` + fmt.Sprintf("%v", this.Args) + `,`,
		`ActionTimeout:` + fmt.Sprintf("%v", this.ActionTimeout) + `,`,
		`}`,
	}, "")
	return s
}
func (this *ExecuteVtctlCommandResponse) String() string {
	if this == nil {
		return "nil"
	}
	s := strings.Join([]string{`&ExecuteVtctlCommandResponse{`,
		`Event:` + strings.Replace(fmt.Sprintf("%v", this.Event), "Event", "logutil.Event", 1) + `,`,
		`}`,
	}, "")
	return s
}
func valueToStringVtctldata(v interface{}) string {
	rv := reflect.ValueOf(v)
	if rv.IsNil() {
		return "nil"
	}
	pv := reflect.Indirect(rv).Interface()
	return fmt.Sprintf("*%v", pv)
}
func (m *ExecuteVtctlCommandRequest) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowVtctldata
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= uint64(b&0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: ExecuteVtctlCommandRequest: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: ExecuteVtctlCommandRequest: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Args", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowVtctldata
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= uint64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthVtctldata
			}
			postIndex := iNdEx + intStringLen
			if postIndex < 0 {
				return ErrInvalidLengthVtctldata
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Args = append(m.Args, string(dAtA[iNdEx:postIndex]))
			iNdEx = postIndex
		case 2:
			if wireType != 0 {
				return fmt.Errorf("proto: wrong wireType = %d for field ActionTimeout", wireType)
			}
			m.ActionTimeout = 0
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowVtctldata
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				m.ActionTimeout |= int64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
		default:
			iNdEx = preIndex
			skippy, err := skipVtctldata(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthVtctldata
			}
			if (iNdEx + skippy) < 0 {
				return ErrInvalidLengthVtctldata
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func (m *ExecuteVtctlCommandResponse) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowVtctldata
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= uint64(b&0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: ExecuteVtctlCommandResponse: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: ExecuteVtctlCommandResponse: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Event", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowVtctldata
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= int(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthVtctldata
			}
			postIndex := iNdEx + msglen
			if postIndex < 0 {
				return ErrInvalidLengthVtctldata
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			if m.Event == nil {
				m.Event = &logutil.Event{}
			}
			if err := m.Event.Unmarshal(dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipVtctldata(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthVtctldata
			}
			if (iNdEx + skippy) < 0 {
				return ErrInvalidLengthVtctldata
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func skipVtctldata(dAtA []byte) (n int, err error) {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return 0, ErrIntOverflowVtctldata
			}
			if iNdEx >= l {
				return 0, io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		wireType := int(wire & 0x7)
		switch wireType {
		case 0:
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowVtctldata
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				iNdEx++
				if dAtA[iNdEx-1] < 0x80 {
					break
				}
			}
			return iNdEx, nil
		case 1:
			iNdEx += 8
			return iNdEx, nil
		case 2:
			var length int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowVtctldata
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				length |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if length < 0 {
				return 0, ErrInvalidLengthVtctldata
			}
			iNdEx += length
			if iNdEx < 0 {
				return 0, ErrInvalidLengthVtctldata
			}
			return iNdEx, nil
		case 3:
			for {
				var innerWire uint64
				var start int = iNdEx
				for shift := uint(0); ; shift += 7 {
					if shift >= 64 {
						return 0, ErrIntOverflowVtctldata
					}
					if iNdEx >= l {
						return 0, io.ErrUnexpectedEOF
					}
					b := dAtA[iNdEx]
					iNdEx++
					innerWire |= (uint64(b) & 0x7F) << shift
					if b < 0x80 {
						break
					}
				}
				innerWireType := int(innerWire & 0x7)
				if innerWireType == 4 {
					break
				}
				next, err := skipVtctldata(dAtA[start:])
				if err != nil {
					return 0, err
				}
				iNdEx = start + next
				if iNdEx < 0 {
					return 0, ErrInvalidLengthVtctldata
				}
			}
			return iNdEx, nil
		case 4:
			return iNdEx, nil
		case 5:
			iNdEx += 4
			return iNdEx, nil
		default:
			return 0, fmt.Errorf("proto: illegal wireType %d", wireType)
		}
	}
	panic("unreachable")
}

var (
	ErrInvalidLengthVtctldata = fmt.Errorf("proto: negative length found during unmarshaling")
	ErrIntOverflowVtctldata   = fmt.Errorf("proto: integer overflow")
)
