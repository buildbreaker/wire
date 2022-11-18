// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos.kotlin.MessageWithStatus in same_name_enum.proto
import Foundation
import Wire

public struct MessageWithStatus {

    public var unknownFields: Data = .init()

    public init() {
    }

    public enum Status : UInt32, CaseIterable, Codable {

        case A = 1

    }

}

#if !WIRE_REMOVE_EQUATABLE
extension MessageWithStatus : Equatable {
}
#endif

#if !WIRE_REMOVE_HASHABLE
extension MessageWithStatus : Hashable {
}
#endif

extension MessageWithStatus : ProtoMessage {
    public static func protoMessageTypeURL() -> String {
        return "type.googleapis.com/squareup.protos.kotlin.MessageWithStatus"
    }
}

extension MessageWithStatus : Proto2Codable {
    public init(from reader: ProtoReader) throws {
        let token = try reader.beginMessage()
        while let tag = try reader.nextTag(token: token) {
            switch tag {
            default: try reader.readUnknownField(tag: tag)
            }
        }
        self.unknownFields = try reader.endMessage(token: token)

    }

    public func encode(to writer: ProtoWriter) throws {
        try writer.writeUnknownFields(unknownFields)
    }
}

#if !WIRE_REMOVE_CODABLE
extension MessageWithStatus : Codable {
    public enum CodingKeys : CodingKey {
    }
}
#endif
