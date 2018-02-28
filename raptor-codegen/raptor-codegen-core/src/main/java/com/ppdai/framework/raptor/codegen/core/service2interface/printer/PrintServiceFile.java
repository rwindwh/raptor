package com.ppdai.framework.raptor.codegen.core.service2interface.printer;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;
import com.ppdai.framework.raptor.codegen.core.utils.CommonUtils;

import java.util.List;
import java.util.Map;

public final class PrintServiceFile extends AbstractPrint {

    private Map<String, String> pojoTypeCache;

    private List<MethodDescriptorProto> serviceMethods;

    public PrintServiceFile(String fileRootPath, String sourcePackageName, String className) {
        super(fileRootPath, sourcePackageName, className);
    }

    public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
        this.pojoTypeCache = pojoTypeCache;
    }

    public void setServiceMethods(List<MethodDescriptorProto> serviceMethods) {
        this.serviceMethods = serviceMethods;
    }

    @Override
    protected List<String> collectFileData() {
        String className = super.getClassName();
        String packageName = super.getSourcePackageName().toLowerCase();
        List<String> fileData = Lists.newArrayList("// Generated by the raptor-codegen .  DO NOT EDIT!");
        if (!("".equals(packageName))) {
            fileData.add("package " + packageName + ";");
        }
        fileData.add("");

        fileData.add("import com.ppdai.framework.raptor.annotation.RaptorInterface;");
        fileData.add("");
        fileData.add("@RaptorInterface");
        fileData.add("public interface " + className + "{");
        for (MethodDescriptorProto method : serviceMethods) {
            String outPutType = method.getOutputType();
            String inPutType = method.getInputType();
            String methodName = method.getName();
            inPutType = CommonUtils.findPojoTypeFromCache(inPutType, pojoTypeCache);
            outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
            String inputValue = inputTypeProcess(CommonUtils.findNotIncludePackageType(inPutType));

            String methodStr = generateMethod(method, inPutType, outPutType, methodName, inputValue);
            fileData.add(methodStr);
        }
        fileData.add("}");
        return fileData;
    }

    private String inputTypeProcess(String inputType) {
        char[] temp = new char[1];
        temp[0] = inputType.charAt(0);
        if (64 < temp[0] && temp[0] < 91)
            return inputType.replaceFirst(new String(temp), new String(temp).toLowerCase());
        return inputType;
    }

    private String generateGrpcStream(MethodDescriptorProto method, String inPutType,
                                      String outPutType) {
        String format =
                "@com.quancheng.saluki.GrpcMethodType(methodType=%s,requestType=%s,responseType=%s)";
        if (method.getServerStreaming() && method.getClientStreaming()) {
            String stream = String.format(format, "io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING",
                    inPutType + ".class", outPutType + ".class");
            return stream;
        } else {
            if (!method.getServerStreaming() && method.getClientStreaming()) {
                String stream =
                        String.format(format, "io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING",
                                inPutType + ".class", outPutType + ".class");
                return stream;
            } else if (method.getServerStreaming() && !method.getClientStreaming()) {
                String stream =
                        String.format(format, "io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING",
                                inPutType + ".class", outPutType + ".class");
                return stream;
            }
            return String.format(format, "io.grpc.MethodDescriptor.MethodType.UNARY",
                    inPutType + ".class", outPutType + ".class");
        }
    }

    private String generateMethod(MethodDescriptorProto method, String inPutType, String outPutType,
                                  String methodName, String inputValue) {
        String methodStr =
                "   " + outPutType + " " + methodName + "(" + inPutType + " " + inputValue + ");";
        return methodStr;
    }


}
