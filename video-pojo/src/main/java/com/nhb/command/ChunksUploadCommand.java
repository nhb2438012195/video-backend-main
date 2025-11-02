package com.nhb.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChunksUploadCommand {
   private String fileName;
    private  String uploadKey;
    private   Integer chunkIndex;
    private  String username;
}
