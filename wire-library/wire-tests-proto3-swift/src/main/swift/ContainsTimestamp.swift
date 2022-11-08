// Code generated by Wire protocol buffer compiler, do not edit.
// Source: squareup.protos3.kotlin.contains_timestamp.ContainsTimestamp in contains_timestamp.proto
import Foundation
import Wire

public struct ContainsTimestamp {

    public var timestamp: Timestamp?
    public var unknownFields: Data = .init()

    public init(timestamp: Timestamp? = nil) {
        self.timestamp = timestamp
    }

}

#if !WIRE_REMOVE_EQUATABLE
extension ContainsTimestamp : Equatable {
}
#endif

#if !WIRE_REMOVE_HASHABLE
extension ContainsTimestamp : Hashable {
}
#endif

extension ContainsTimestamp : ProtoMessage {
    public static func protoMessageTypeURL() -> String {
        return "type.googleapis.com/squareup.protos3.kotlin.contains_timestamp.ContainsTimestamp"
    }
}

extension ContainsTimestamp : Proto3Codable {
    public init(from reader: ProtoReader) throws {
        var timestamp: Timestamp? = nil

        let token = try reader.beginMessage()
        while let tag = try reader.nextTag(token: token) {
            switch tag {
            case 1: timestamp = try reader.decode(Timestamp.self)
            default: try reader.readUnknownField(tag: tag)
            }
        }
        self.unknownFields = try reader.endMessage(token: token)

        self.timestamp = timestamp
    }

    public func encode(to writer: ProtoWriter) throws {
        try writer.encode(tag: 1, value: self.timestamp)
        try writer.writeUnknownFields(unknownFields)
    }
}

#if !WIRE_REMOVE_CODABLE
extension ContainsTimestamp : Codable {
    public enum CodingKeys : String, CodingKey {

        case timestamp

    }
}
#endif
