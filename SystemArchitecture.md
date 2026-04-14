## System Architecture

```mermaid
flowchart LR

    %% ===== CLIENT =====
    subgraph Client
        A[Frontend]
    end

    %% ===== GATEWAY =====
    subgraph Gateway
        B[API Gateway]
    end

    %% ===== SERVICES =====
    subgraph Services
        C[Auth Service]
        D[Event Service - Producer]
        E[Product Service]
        F[User Service]
        G[Recommendation Service]
        I[Analytics Service - Consumer]
    end

    %% ===== MESSAGING =====
    subgraph Messaging
        H[(Kafka)]
    end

    %% ===== STORAGE =====
    subgraph Storage
        J[(MongoDB - Events)]
        J2[(MongoDB - Products)]
        J3[(MongoDB - Users)]
        K[(Redis - Cache & Realtime)]
    end

    %% ===== FLOW =====
    A --> B

    %% Auth flow
    B --> C

    %% Core services
    B --> D
    B --> E
    B --> F
    B --> G

    %% Event streaming
    D -- produce --> H
    H -- consume --> I

    %% Analytics storage
    I --> J
    I --> K

    %% Product storage + cache
    E --> J2
    E <--> K

    %% User storage
    F --> J3

    %% Recommendation
    G --> K
    G --> J