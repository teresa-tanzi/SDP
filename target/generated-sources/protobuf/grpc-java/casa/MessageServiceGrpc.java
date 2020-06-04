package casa;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 * <pre>
 * definizione del servizio
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: MessageService.proto")
public final class MessageServiceGrpc {

  private MessageServiceGrpc() {}

  public static final String SERVICE_NAME = "casa.MessageService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<casa.MessageServiceOuterClass.HelloRequest,
      casa.MessageServiceOuterClass.HelloResponse> METHOD_HELLO =
      io.grpc.MethodDescriptor.<casa.MessageServiceOuterClass.HelloRequest, casa.MessageServiceOuterClass.HelloResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "casa.MessageService", "hello"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.HelloRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.HelloResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier("hello"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<casa.MessageServiceOuterClass.GoodbyeRequest,
      casa.MessageServiceOuterClass.GoodbyeResponse> METHOD_GOODBYE =
      io.grpc.MethodDescriptor.<casa.MessageServiceOuterClass.GoodbyeRequest, casa.MessageServiceOuterClass.GoodbyeResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "casa.MessageService", "goodbye"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.GoodbyeRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.GoodbyeResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier("goodbye"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<casa.MessageServiceOuterClass.InvioStat,
      casa.MessageServiceOuterClass.AckStat> METHOD_LOCAL_STAT =
      io.grpc.MethodDescriptor.<casa.MessageServiceOuterClass.InvioStat, casa.MessageServiceOuterClass.AckStat>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "casa.MessageService", "localStat"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.InvioStat.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.AckStat.getDefaultInstance()))
          .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier("localStat"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<casa.MessageServiceOuterClass.ElectionRequest,
      casa.MessageServiceOuterClass.ElectionResponse> METHOD_ELECTION =
      io.grpc.MethodDescriptor.<casa.MessageServiceOuterClass.ElectionRequest, casa.MessageServiceOuterClass.ElectionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "casa.MessageService", "election"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.ElectionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.ElectionResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier("election"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<casa.MessageServiceOuterClass.ExtraPowerRequest,
      casa.MessageServiceOuterClass.ExtraPowerResponse> METHOD_EXTRA_POWER =
      io.grpc.MethodDescriptor.<casa.MessageServiceOuterClass.ExtraPowerRequest, casa.MessageServiceOuterClass.ExtraPowerResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "casa.MessageService", "extraPower"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.ExtraPowerRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              casa.MessageServiceOuterClass.ExtraPowerResponse.getDefaultInstance()))
          .setSchemaDescriptor(new MessageServiceMethodDescriptorSupplier("extraPower"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MessageServiceStub newStub(io.grpc.Channel channel) {
    return new MessageServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MessageServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new MessageServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MessageServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new MessageServiceFutureStub(channel);
  }

  /**
   * <pre>
   * definizione del servizio
   * </pre>
   */
  public static abstract class MessageServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * un nuovo nodo si presenta nella rete
     * </pre>
     */
    public void hello(casa.MessageServiceOuterClass.HelloRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.HelloResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_HELLO, responseObserver);
    }

    /**
     * <pre>
     * un nodo chiede di uscire dalla rete
     * </pre>
     */
    public void goodbye(casa.MessageServiceOuterClass.GoodbyeRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.GoodbyeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GOODBYE, responseObserver);
    }

    /**
     * <pre>
     * un nodo manda le sue statistiche locali man mano che le calcola
     * </pre>
     */
    public void localStat(casa.MessageServiceOuterClass.InvioStat request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.AckStat> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LOCAL_STAT, responseObserver);
    }

    /**
     * <pre>
     * quando il coordinatore esce elegge un altro coordinatore
     * </pre>
     */
    public void election(casa.MessageServiceOuterClass.ElectionRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ElectionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ELECTION, responseObserver);
    }

    /**
     * <pre>
     * richiesta della corrente extra
     * </pre>
     */
    public void extraPower(casa.MessageServiceOuterClass.ExtraPowerRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ExtraPowerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_EXTRA_POWER, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_HELLO,
            asyncUnaryCall(
              new MethodHandlers<
                casa.MessageServiceOuterClass.HelloRequest,
                casa.MessageServiceOuterClass.HelloResponse>(
                  this, METHODID_HELLO)))
          .addMethod(
            METHOD_GOODBYE,
            asyncUnaryCall(
              new MethodHandlers<
                casa.MessageServiceOuterClass.GoodbyeRequest,
                casa.MessageServiceOuterClass.GoodbyeResponse>(
                  this, METHODID_GOODBYE)))
          .addMethod(
            METHOD_LOCAL_STAT,
            asyncUnaryCall(
              new MethodHandlers<
                casa.MessageServiceOuterClass.InvioStat,
                casa.MessageServiceOuterClass.AckStat>(
                  this, METHODID_LOCAL_STAT)))
          .addMethod(
            METHOD_ELECTION,
            asyncUnaryCall(
              new MethodHandlers<
                casa.MessageServiceOuterClass.ElectionRequest,
                casa.MessageServiceOuterClass.ElectionResponse>(
                  this, METHODID_ELECTION)))
          .addMethod(
            METHOD_EXTRA_POWER,
            asyncUnaryCall(
              new MethodHandlers<
                casa.MessageServiceOuterClass.ExtraPowerRequest,
                casa.MessageServiceOuterClass.ExtraPowerResponse>(
                  this, METHODID_EXTRA_POWER)))
          .build();
    }
  }

  /**
   * <pre>
   * definizione del servizio
   * </pre>
   */
  public static final class MessageServiceStub extends io.grpc.stub.AbstractStub<MessageServiceStub> {
    private MessageServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MessageServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MessageServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MessageServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * un nuovo nodo si presenta nella rete
     * </pre>
     */
    public void hello(casa.MessageServiceOuterClass.HelloRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.HelloResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_HELLO, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * un nodo chiede di uscire dalla rete
     * </pre>
     */
    public void goodbye(casa.MessageServiceOuterClass.GoodbyeRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.GoodbyeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GOODBYE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * un nodo manda le sue statistiche locali man mano che le calcola
     * </pre>
     */
    public void localStat(casa.MessageServiceOuterClass.InvioStat request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.AckStat> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LOCAL_STAT, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * quando il coordinatore esce elegge un altro coordinatore
     * </pre>
     */
    public void election(casa.MessageServiceOuterClass.ElectionRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ElectionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ELECTION, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * richiesta della corrente extra
     * </pre>
     */
    public void extraPower(casa.MessageServiceOuterClass.ExtraPowerRequest request,
        io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ExtraPowerResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXTRA_POWER, getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * definizione del servizio
   * </pre>
   */
  public static final class MessageServiceBlockingStub extends io.grpc.stub.AbstractStub<MessageServiceBlockingStub> {
    private MessageServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MessageServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MessageServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MessageServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * un nuovo nodo si presenta nella rete
     * </pre>
     */
    public casa.MessageServiceOuterClass.HelloResponse hello(casa.MessageServiceOuterClass.HelloRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_HELLO, getCallOptions(), request);
    }

    /**
     * <pre>
     * un nodo chiede di uscire dalla rete
     * </pre>
     */
    public casa.MessageServiceOuterClass.GoodbyeResponse goodbye(casa.MessageServiceOuterClass.GoodbyeRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GOODBYE, getCallOptions(), request);
    }

    /**
     * <pre>
     * un nodo manda le sue statistiche locali man mano che le calcola
     * </pre>
     */
    public casa.MessageServiceOuterClass.AckStat localStat(casa.MessageServiceOuterClass.InvioStat request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LOCAL_STAT, getCallOptions(), request);
    }

    /**
     * <pre>
     * quando il coordinatore esce elegge un altro coordinatore
     * </pre>
     */
    public casa.MessageServiceOuterClass.ElectionResponse election(casa.MessageServiceOuterClass.ElectionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ELECTION, getCallOptions(), request);
    }

    /**
     * <pre>
     * richiesta della corrente extra
     * </pre>
     */
    public casa.MessageServiceOuterClass.ExtraPowerResponse extraPower(casa.MessageServiceOuterClass.ExtraPowerRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXTRA_POWER, getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * definizione del servizio
   * </pre>
   */
  public static final class MessageServiceFutureStub extends io.grpc.stub.AbstractStub<MessageServiceFutureStub> {
    private MessageServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private MessageServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MessageServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new MessageServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * un nuovo nodo si presenta nella rete
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<casa.MessageServiceOuterClass.HelloResponse> hello(
        casa.MessageServiceOuterClass.HelloRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_HELLO, getCallOptions()), request);
    }

    /**
     * <pre>
     * un nodo chiede di uscire dalla rete
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<casa.MessageServiceOuterClass.GoodbyeResponse> goodbye(
        casa.MessageServiceOuterClass.GoodbyeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GOODBYE, getCallOptions()), request);
    }

    /**
     * <pre>
     * un nodo manda le sue statistiche locali man mano che le calcola
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<casa.MessageServiceOuterClass.AckStat> localStat(
        casa.MessageServiceOuterClass.InvioStat request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LOCAL_STAT, getCallOptions()), request);
    }

    /**
     * <pre>
     * quando il coordinatore esce elegge un altro coordinatore
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<casa.MessageServiceOuterClass.ElectionResponse> election(
        casa.MessageServiceOuterClass.ElectionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ELECTION, getCallOptions()), request);
    }

    /**
     * <pre>
     * richiesta della corrente extra
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<casa.MessageServiceOuterClass.ExtraPowerResponse> extraPower(
        casa.MessageServiceOuterClass.ExtraPowerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXTRA_POWER, getCallOptions()), request);
    }
  }

  private static final int METHODID_HELLO = 0;
  private static final int METHODID_GOODBYE = 1;
  private static final int METHODID_LOCAL_STAT = 2;
  private static final int METHODID_ELECTION = 3;
  private static final int METHODID_EXTRA_POWER = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MessageServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MessageServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_HELLO:
          serviceImpl.hello((casa.MessageServiceOuterClass.HelloRequest) request,
              (io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.HelloResponse>) responseObserver);
          break;
        case METHODID_GOODBYE:
          serviceImpl.goodbye((casa.MessageServiceOuterClass.GoodbyeRequest) request,
              (io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.GoodbyeResponse>) responseObserver);
          break;
        case METHODID_LOCAL_STAT:
          serviceImpl.localStat((casa.MessageServiceOuterClass.InvioStat) request,
              (io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.AckStat>) responseObserver);
          break;
        case METHODID_ELECTION:
          serviceImpl.election((casa.MessageServiceOuterClass.ElectionRequest) request,
              (io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ElectionResponse>) responseObserver);
          break;
        case METHODID_EXTRA_POWER:
          serviceImpl.extraPower((casa.MessageServiceOuterClass.ExtraPowerRequest) request,
              (io.grpc.stub.StreamObserver<casa.MessageServiceOuterClass.ExtraPowerResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class MessageServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MessageServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return casa.MessageServiceOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MessageService");
    }
  }

  private static final class MessageServiceFileDescriptorSupplier
      extends MessageServiceBaseDescriptorSupplier {
    MessageServiceFileDescriptorSupplier() {}
  }

  private static final class MessageServiceMethodDescriptorSupplier
      extends MessageServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MessageServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MessageServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MessageServiceFileDescriptorSupplier())
              .addMethod(METHOD_HELLO)
              .addMethod(METHOD_GOODBYE)
              .addMethod(METHOD_LOCAL_STAT)
              .addMethod(METHOD_ELECTION)
              .addMethod(METHOD_EXTRA_POWER)
              .build();
        }
      }
    }
    return result;
  }
}
